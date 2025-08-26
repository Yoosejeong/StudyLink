<script lang="ts">
  import { goto } from '$app/navigation';
  import { isLoggedIn } from '$lib/stores/auth';
  import { fetchWithAuth } from '$lib/api/fetchWithAuth';
  import { onDestroy, onMount } from 'svelte';

  export let handleLogin: () => void;
  export let handleLogout: () => Promise<void>;

  // 이동 버튼
  function goToCreatePage() { goto('/studies/new'); }

  // --- 타입들 ---
  type ApiEnvelope<T> = { isSuccess: boolean; code: string; message: string; result: T };
  type HeaderProfile = { profileUrl?: string | null; profileURL?: string | null; expiresAt?: string | null };
  type PresignPutResult = { method:'PUT'; url:string; headers?:Record<string,string>; key:string; expiresAt:string; };
  type PresignGetResult = { method:'GET'; url:string; key:string; expiresAt:string; };

  // --- 아바타 상태 ---
  let avatarUrl: string | null = null;
  let avatarExpiresAt: string | null = null;

  // --- 로컬 캐시 (presigned GET을 만료 전까지 재사용) ---
  const LS_KEY = 'meAvatarPresigned';
  const SAFETY_MS = 30_000; // 만료 30초 전에 재발급 요구

  type AvatarCache = { url: string; exp: number }; // exp = epoch ms

  function readAvatarCache(): AvatarCache | null {
    try {
      const raw = localStorage.getItem(LS_KEY);
      if (!raw) return null;
      const c = JSON.parse(raw) as AvatarCache;
      if (!c?.url || !c?.exp) return null;
      if (Date.now() + SAFETY_MS >= c.exp) return null; // 곧 만료 → 사용하지 않음
      return c;
    } catch { return null; }
  }

  function writeAvatarCache(url: string, expISO: string) {
    try {
      const exp = new Date(expISO).getTime();
      localStorage.setItem(LS_KEY, JSON.stringify({ url, exp }));
    } catch {}
  }

  function clearAvatarCache() { localStorage.removeItem(LS_KEY); }

  // 서버에서 presigned GET 재발급
  async function renewAvatar(): Promise<boolean> {
    const res = await fetchWithAuth('/api/s3/presign/me', { method:'GET' });
    if (!res.ok) return false;
    const env: ApiEnvelope<HeaderProfile> = await res.json();
    const url = env?.result?.profileUrl ?? env?.result?.profileURL ?? null;
    const exp = env?.result?.expiresAt ?? null;
    if (!url || !exp) return false;
    avatarUrl = url;
    avatarExpiresAt = exp;
    writeAvatarCache(url, exp);
    return true;
  }

  // 초기 로드: 캐시 우선, 없으면 1회만 재발급
  async function loadAvatar() {
    const cached = readAvatarCache();
    if (cached) {
      avatarUrl = cached.url;
      avatarExpiresAt = new Date(cached.exp).toISOString();
      return; // ✅ 서버 호출 생략
    }
    await renewAvatar(); // 캐시 없거나 만료 → 1회 발급
  }

  // 로그인 상태 변화에 맞춰 로드/정리
  onMount(() => {
    const unsub = isLoggedIn.subscribe(v => {
      if (v) {
        void loadAvatar();
      } else {
        avatarUrl = null;
        avatarExpiresAt = null;
        clearAvatarCache();
      }
    });
    onDestroy(unsub);
  });

  // --- 아바타 팝오버 & 모달 상태 ---
  let menuOpen = false;
  let modalOpen = false;

  function toggleMenu() { menuOpen = !menuOpen; }
  function openModal() { modalOpen = true; menuOpen = false; }
  function closeModal() { modalOpen = false; resetUploadState(); }

  // --- 업로드 로직 ---
  let file: File | null = null;
  let uploading = false;
  let progress = 0;
  let message = '';
  let etag: string | null = null;
  let previewUrl: string | null = null;

  function resetUploadState() {
    file = null; uploading = false; progress = 0; message = ''; etag = null; previewUrl = null;
  }

  function onPick(e: Event) {
    const input = e.target as HTMLInputElement;
    file = input.files?.[0] ?? null;
    progress = 0; message = ''; etag = null; previewUrl = null;
  }

  function extFromType(type: string): string {
    const m: Record<string,string> = {
      'image/jpeg':'jpg', 'image/png':'png', 'image/webp':'webp',
      'image/gif':'gif', 'image/avif':'avif'
    };
    return m[type] ?? 'bin';
  }

  // 금지 헤더(Host 등) 제외하고 필요한 헤더만 설정
  function xhrPutWithProgress(url: string, file: File, headers: Record<string,string>): Promise<string|null> {
    return new Promise((resolve, reject) => {
      const xhr = new XMLHttpRequest();
      xhr.open('PUT', url);

      const ct = headers['Content-Type'] ?? headers['content-type'] ?? file.type;
      if (ct) xhr.setRequestHeader('Content-Type', ct);

      const isAllowed = (k:string) =>
        /^x-amz-/i.test(k) ||
        /^content-md5$/i.test(k) ||
        /^cache-control$/i.test(k) ||
        /^content-disposition$/i.test(k) ||
        /^content-encoding$/i.test(k) ||
        /^content-type$/i.test(k);

      for (const [k, v] of Object.entries(headers)) {
        if (!isAllowed(k)) continue;
        if (/^content-type$/i.test(k)) continue;
        xhr.setRequestHeader(k, v);
      }

      xhr.upload.addEventListener('progress', (e) => {
        if (e.lengthComputable) progress = Math.round((e.loaded / e.total) * 100);
      });

      xhr.onload = () => {
        if (xhr.status >= 200 && xhr.status < 300) resolve(xhr.getResponseHeader('ETag'));
        else reject(new Error(`HTTP ${xhr.status}: ${xhr.responseText || '업로드 실패'}`));
      };
      xhr.onerror = () => reject(new Error('네트워크 오류'));
      xhr.send(file);
    });
  }

  // 이미지 만료 등으로 에러 났을 때 1회만 재발급 (무한루프 방지)
  let renewing = false;
  async function onImgError() {
    if (renewing) return;
    renewing = true;
    const ok = await renewAvatar();
    renewing = false;
    // ok면 avatarUrl 갱신되어 이미지가 자동 재로딩됨
  }

  async function uploadAvatar() {
    if (!file) { message = '파일을 선택하세요.'; return; }
    uploading = true; message = '프리사인 발급 중...';

    const filename = `${crypto.randomUUID()}_avatar.${extFromType(file.type)}`;

    // 1) presign PUT (서버에서 presign만 생성)
    const putRes = await fetchWithAuth('/api/s3/presign/upload', {
      method:'POST',
      headers:{ 'Content-Type':'application/json' },
      body: JSON.stringify({ filename, contentType:file.type, contentLength:file.size })
    });
    if (!putRes.ok) { uploading = false; message = `presign 실패: ${putRes.status}`; return; }
    const envPut: ApiEnvelope<PresignPutResult> = await putRes.json();
    const putUrl = envPut?.result?.url;
    const headers = envPut?.result?.headers ?? {};
    const key = envPut?.result?.key;
    if (!putUrl || !/^https?:\/\//.test(putUrl) || !key) {
      uploading = false; message = 'presign 데이터 이상'; return;
    }

    // 2) S3 업로드 (브라우저가 S3로 직접 PUT 요청 )
    try {
      message = 'S3 업로드 중...';
      await xhrPutWithProgress(putUrl, file, headers);
    } catch (e:any) {
      uploading = false; message = `업로드 실패: ${e?.message ?? e}`; return;
    }

    // 3) presign GET → 공식 URL로 교체 + 캐시 저장
    try {
      const getRes = await fetchWithAuth('/api/s3/presign/me', { method: 'GET' });
      if (getRes.ok) {
        const envGet: ApiEnvelope<PresignGetResult> = await getRes.json();
        const officialUrl = envGet?.result?.url ?? null;
        const exp = envGet?.result?.expiresAt ?? null;
        if (officialUrl) {
          avatarUrl = officialUrl;
          avatarExpiresAt = exp ?? null;
          if (officialUrl && exp) writeAvatarCache(officialUrl, exp); // ★ 캐시에 저장
        }
      } else {
        await renewAvatar(); // 실패 시 서버 재조회
      }
    } finally {
      closeModal();
    }

    uploading = false; message = '완료!';
  }
</script>

<header class="bg-white shadow-sm" >
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
    <div class="flex justify-between items-center h-16">
      <div class="flex items-center">
        <a href="/" class="text-2xl font-bold text-blue-600 hover:underline">StudyLink</a>
      </div>

      <div class="flex items-center space-x-3">
        <button
          class="px-4 py-1 text-sm rounded-full border border-gray-300 bg-white shadow-sm hover:bg-gray-50"
          on:click={goToCreatePage}
        >
          팀원 모집
        </button>

        {#if $isLoggedIn}
          <button
            class="px-4 py-1 text-sm rounded-full bg-gray-600 text-white shadow hover:bg-gray-700"
            on:click={handleLogout}
          >
            로그아웃
          </button>

          <!-- 아바타 -->
          <div class="relative" data-avatar>
            {#if avatarUrl}
              <!-- 브라우저가 S3로 직접 GET 요청 -->
              <img
                src={avatarUrl}
                alt="내 프로필"
                class="size-8.5 rounded-full object-cover cursor-pointer block"
                style="aspect-ratio: 1 / 1;"
                loading="lazy" decoding="async"
                on:error={onImgError}
                on:click={() => (menuOpen = !menuOpen)}
              />
            {:else}
              <div
                class="size-8.5 rounded-full bg-gray-300 cursor-pointer"
                style="aspect-ratio: 1 / 1;"
                aria-label="no avatar"
                on:click={() => (menuOpen = !menuOpen)}
              />
            {/if}

            {#if menuOpen}
              <div
                class="absolute right-0 mt-2 w-40 rounded-lg border border-gray-200 bg-white shadow-lg z-20"
                data-menu
              >
                <button
                  class="w-full text-left px-3 py-2 text-sm hover:bg-gray-50"
                  on:click={() => { menuOpen = false; modalOpen = true; }}
                >
                  프로필 편집
                </button>
              </div>
            {/if}
          </div>
        {:else}
          <button
            class="px-4 py-1 text-sm rounded-full bg-blue-600 text-white shadow hover:bg-blue-700"
            on:click={handleLogin}
          >
            로그인
          </button>
        {/if}
      </div>
    </div>
  </div>

  <!-- 업로드 모달 -->
  {#if modalOpen}
    <div class="fixed inset-0 z-30 flex items-center justify-center">
      <div class="absolute inset-0 bg-black/30" on:click={() => (modalOpen = false)}></div>

      <div class="relative z-40 w-full max-w-md rounded-xl bg-white p-5 shadow-xl">
        <h3 class="text-lg font-semibold mb-3">프로필 이미지 업로드</h3>

        <div class="space-y-3">
          <!-- 실제 input은 숨기고 버튼으로만 트리거 -->
          <input
            id="avatarFile"
            type="file"
            accept="image/*"
            class="sr-only"
            on:change={onPick}
          />

          <!-- 테두리 상자 + 파란 버튼 + 상태 텍스트 -->
          <div class="rounded-lg border border-gray-200 p-4">
            <div class="flex items-center gap-3">
              <button
                type="button"
                class="inline-flex items-center gap-2 rounded-md bg-blue-600 px-3 py-1.5 text-white
                       hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-400 disabled:opacity-50"
                on:click={() => document.getElementById('avatarFile')?.click()}
                disabled={uploading}
              >
                파일 선택
              </button>

              {#if file}
                <span class="truncate max-w-[14rem] text-sm text-gray-800">{file.name}</span>
                <span class="text-xs text-gray-500">{file.type || '파일'}</span>
              {:else}
                <span class="text-sm text-gray-500">선택된 파일 없음</span>
              {/if}
            </div>
          </div>

          <!-- 액션 버튼 -->
          <div class="flex items-center gap-2 justify-end">
            <button class="px-4 py-1 text-sm rounded bg-gray-100" on:click={() => (modalOpen = false)}>취소</button>
            <button
              class="px-4 py-1 text-sm rounded bg-blue-600 text-white disabled:opacity-50"
              on:click={uploadAvatar}
              disabled={!file || uploading}
            >
              {uploading ? '업로드 중…' : '업로드'}
            </button>
          </div>

          {#if uploading}
            <div class="text-sm text-gray-700">업로드 중… {progress}%</div>
          {/if}
          {#if message}
            <p class="text-sm text-gray-600">{message}</p>
          {/if}
          {#if etag}
            <p class="text-xs text-gray-500">ETag: {etag}</p>
          {/if}
        </div>
      </div>
    </div>
  {/if}
</header>

<style>
  :global(.shadow-lg) { box-shadow: 0 10px 20px rgba(0,0,0,.08), 0 6px 6px rgba(0,0,0,.06); }
</style>
