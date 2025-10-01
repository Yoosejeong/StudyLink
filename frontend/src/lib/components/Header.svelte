<script lang="ts">
  import { goto } from '$app/navigation';
  import { isLoggedIn } from '$lib/stores/auth';
  import { onDestroy, onMount } from 'svelte';
  import { http } from '$lib/api/http';
  import { avatarVersion } from '$lib/stores/avatar';

  // 기본 프로필
  const DEFAULT_AVATAR = '/avatars/default.jpg';

  // 이동 버튼
  function goToCreatePage() { goto('/studies/new'); }
  function goToMyApplications() { menuOpen = false; goto('/mypage/applications'); }
  function goToProfile() { menuOpen = false; goto('/mypage/profile'); }

  // --- 타입들 ---
  type ApiEnvelope<T> = { isSuccess: boolean; code: string; message: string; result: T };
  type HeaderProfile = { profileUrl?: string | null; profileURL?: string | null; expiresAt?: string | null };

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
    try { localStorage.setItem(LS_KEY, JSON.stringify({ url, exp: new Date(expISO).getTime() })); } catch {}
  }
  function clearAvatarCache() { localStorage.removeItem(LS_KEY); }

  // 서버에서 presigned GET 재발급
  async function renewAvatar(): Promise<boolean> {
    const res = await http.get('/api/s3/presign/me');
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
      return;
    }
    await renewAvatar();
  }

  // 로그인 상태 변화에 맞춰 로드/정리
  onMount(() => {
    const unsub = isLoggedIn.subscribe(v => {
      if (v) void loadAvatar();
      else { avatarUrl = null; avatarExpiresAt = null; clearAvatarCache(); }
    });
    // ✅ 프로필 변경 신호 오면 캐시 지우고 재발급
    const unsubVer = avatarVersion.subscribe(async () => {
      clearAvatarCache();
      await renewAvatar();
    });

    onDestroy(() => { unsubLogin(); unsubVer(); });
  });

  // 드롭다운 상태
  let menuOpen = false;
  function toggleMenu() { menuOpen = !menuOpen; }

  // 이미지 에러 시: 1) 재발급 시도 2) 실패하면 기본 이미지로 폴백
  let renewing = false;
  async function onImgError(e: Event) {
    const img = e.target as HTMLImageElement;
    if (renewing) return;
    renewing = true;
    const ok = await renewAvatar();
    renewing = false;
    if (ok && avatarUrl) img.src = avatarUrl;
    else img.src = DEFAULT_AVATAR;
  }

  // 부모에서 내려주는 핸들러들
  export let handleLogin: () => void;
  export let handleLogout: () => Promise<void>;
</script>

<header class="bg-white shadow-sm">
  <div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
    <div class="flex h-16 items-center justify-between">
      <div class="flex items-center">
        <a href="/" class="text-2xl font-bold text-blue-600 hover:underline">StudyLink</a>
      </div>

      <div class="flex items-center space-x-2 sm:space-x-3">
        <!-- 팀원 모집: 고스트 버튼 -->
        <button
          class="rounded-full px-4 py-1 text-sm font-semibold text-gray-700 hover:bg-gray-100 focus:ring-2 focus:ring-gray-300 focus:outline-none"
          on:click={goToCreatePage}
        >
          팀원 모집
        </button>

        {#if $isLoggedIn}
          <!-- 아바타 & 드롭다운 (프로필 편집 모달 제거, 마이페이지로 이동) -->
          <div class="relative" data-avatar>
            <img
              src={avatarUrl || DEFAULT_AVATAR}
              alt="내 프로필"
              class="block size-8.5 cursor-pointer rounded-full object-cover"
              style="aspect-ratio: 1 / 1;"
              loading="lazy"
              decoding="async"
              on:error={onImgError}
              on:click={toggleMenu}
              aria-haspopup="menu"
              aria-expanded={menuOpen}
            />

            {#if menuOpen}
              <div
                class="absolute right-0 z-20 mt-2 w-44 overflow-hidden rounded-lg border border-gray-200 bg-white shadow-lg"
                data-menu
              >
                <button
                  class="w-full px-3 py-2 text-left text-sm hover:bg-gray-50"
                  on:click={goToMyApplications}
                >
                  내 지원 목록
                </button>
                <button
                  class="w-full px-3 py-2 text-left text-sm hover:bg-gray-50"
                  on:click={goToProfile}
                >
                  프로필 편집
                </button>
                <button
                  class="w-full px-3 py-2 text-left text-sm text-red-600 hover:bg-red-50"
                  on:click={() => { menuOpen = false; void handleLogout(); }}
                >
                  로그아웃
                </button>
              </div>
            {/if}
          </div>
        {:else}
          <button
            class="rounded-full bg-blue-600 px-4 py-1 text-sm text-white shadow hover:bg-blue-700"
            on:click={handleLogin}
          >
            로그인
          </button>
        {/if}
      </div>
    </div>
  </div>
</header>

<style>
  :global(.shadow-lg) {
    box-shadow: 0 10px 20px rgba(0,0,0,.08), 0 6px 6px rgba(0,0,0,.06);
  }
</style>
