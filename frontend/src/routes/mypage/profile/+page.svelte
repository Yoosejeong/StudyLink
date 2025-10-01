<script lang="ts">
	import { onMount } from 'svelte';
	import { http } from '$lib/api/http';
	import { bumpAvatar } from '$lib/stores/avatar';

	// ---- API 엔드포인트 ----
	const GET_ME_URL = '/api/users';
	const PATCH_NICKNAME_URL = '/api/me/nickname';
	const PRESIGN_ME_URL = '/api/s3/presign/me';
	const PRESIGN_UPLOAD_URL = '/api/s3/presign/upload';
	const PRESIGN_CONFIRM_URL = '/api/s3/presign/confirm';

	// ✅ 모달 상태
	let successOpen = false;
	let successTitle = '';
	let successAction: (() => void) | null = null;

	function openSuccess(t: string, action: (() => void) | null = null) {
		successTitle = t;
		successAction = action;
		successOpen = true;
	}
	function closeSuccess() {
		successOpen = false;
		successTitle = '';
		successAction = null;
	}

	// 기본 프로필 이미지
	const DEFAULT_AVATAR = '/avatars/default.jpg';

	type ApiEnvelope<T> = { isSuccess: boolean; code: string; message: string; result: T };
	type MeDto = { userId: number; email?: string; username: string; nickname: string };
	type HeaderProfile = {
		profileUrl?: string | null;
		profileURL?: string | null;
		expiresAt?: string | null;
	};
	type PresignPutResult = {
		method: 'PUT';
		url: string;
		headers?: Record<string, string>;
		key: string;
		expiresAt: string;
	};

	// ---- 화면 상태 ----
	let loading = true;
	let submitting = false;
	let errorMsg = '';

	// 프로필
	let username = '';
	let email = '';
	let currentNickname = '';
	let nickname = '';

	// 아바타
	let avatarUrl: string | null = null;
	let avatarExpiresAt: string | null = null;

	// presigned GET 로컬 캐시 (만료 전까지 재사용)
	const LS_KEY = 'meAvatarPresigned';
	const SAFETY_MS = 30_000;

	function readAvatarCache() {
		try {
			const raw = localStorage.getItem(LS_KEY);
			if (!raw) return null;
			const c = JSON.parse(raw) as { url: string; exp: number };
			if (!c?.url || !c?.exp) return null;
			if (Date.now() + SAFETY_MS >= c.exp) return null;
			return c;
		} catch {
			return null;
		}
	}
	function writeAvatarCache(url: string, expISO: string) {
		try {
			localStorage.setItem(LS_KEY, JSON.stringify({ url, exp: new Date(expISO).getTime() }));
		} catch {}
	}
	function clearAvatarCache() {
		localStorage.removeItem(LS_KEY);
	}

	// 서버에서 presigned GET 재발급
	async function renewAvatar(): Promise<boolean> {
		const res = await http.get(PRESIGN_ME_URL);
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

	async function loadAvatar() {
		const cached = readAvatarCache();
		if (cached) {
			avatarUrl = cached.url;
			avatarExpiresAt = new Date(cached.exp).toISOString();
			return;
		}
		await renewAvatar();
	}

	// ---- 초기 로딩 ----
	onMount(async () => {
		await Promise.all([loadMe(), loadAvatar()]);
	});

	async function loadMe() {
		loading = true;
		errorMsg = '';
		try {
			const res = await http.get(GET_ME_URL);
			if (!res.ok) {
				errorMsg = `내 정보 조회 실패 (${res.status})`;
				return;
			}
			const env = (await res.json()) as ApiEnvelope<MeDto>;
			const me = env?.result;
			if (!me) {
				errorMsg = '내 정보가 비어 있습니다.';
				return;
			}
			username = me.username ?? '';
			email = me.email ?? '';
			currentNickname = me.nickname ?? '';
			nickname = currentNickname;
		} catch {
			errorMsg = '네트워크 오류';
		} finally {
			loading = false;
		}
	}

	// ---- 닉네임 유효성/저장 ----
	const nicknameRule = { max: 20, regex: /^[a-zA-Z0-9가-힣_ ]+$/ }; // 공백 허용 예시
	$: trimmed = (nickname ?? '').trim();
	$: dirty = trimmed !== currentNickname;
	$: validLen = !dirty || trimmed.length <= nicknameRule.max;
	$: validRegex = !dirty || nicknameRule.regex.test(trimmed);
	$: invalid = dirty && !(validLen && validRegex);
	$: canSubmit = dirty && !invalid && !submitting;

	async function saveNickname() {
		if (!canSubmit) return;
		submitting = true;
		errorMsg = '';
		try {
			const res = await http.patch(PATCH_NICKNAME_URL, { nickname: trimmed });
			if (!res.ok) {
				errorMsg = (await safeMessage(res)) ?? `변경 실패 (${res.status})`;
				return;
			}
			currentNickname = trimmed;
			openSuccess('닉네임이 변경되었습니다.');

		} catch {
			errorMsg = '네트워크 오류';
		} finally {
			submitting = false;
		}
	}

	async function safeMessage(res: Response) {
		const t = await res.text();
		if (!t) return null;
		try {
			return JSON.parse(t)?.message ?? null;
		} catch {
			return null;
		}
	}

	// ---- 아바타 업로드 ----
	let file: File | null = null;
	let uploading = false;
	let uploadProgress = 0;

	function onPick(e: Event) {
		const input = e.target as HTMLInputElement;
		file = input.files?.[0] ?? null;
		if (file) void uploadAvatar();
	}

	function extFromType(type: string): string {
		const m: Record<string, string> = {
			'image/jpeg': 'jpg',
			'image/png': 'png',
			'image/webp': 'webp',
			'image/gif': 'gif',
			'image/avif': 'avif'
		};
		return m[type] ?? 'bin';
	}

	function xhrPutWithProgress(
		url: string,
		file: File,
		headers: Record<string, string>
	): Promise<void> {
		return new Promise((resolve, reject) => {
			const xhr = new XMLHttpRequest();
			xhr.open('PUT', url);

			const ct = headers['Content-Type'] ?? headers['content-type'] ?? file.type;
			if (ct) xhr.setRequestHeader('Content-Type', ct);

			const isAllowed = (k: string) =>
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
				if (e.lengthComputable) uploadProgress = Math.round((e.loaded / e.total) * 100);
			});

			xhr.onload = () =>
				xhr.status >= 200 && xhr.status < 300 ? resolve() : reject(new Error(`HTTP ${xhr.status}`));
			xhr.onerror = () => reject(new Error('네트워크 오류'));
			xhr.send(file);
		});
	}

	async function uploadAvatar() {
		if (!file) return;
		uploading = true;
		uploadProgress = 0;
		errorMsg = '';
		try {
			const filename = `${crypto.randomUUID()}_avatar.${extFromType(file.type)}`;

			// 1) presign PUT
			const putRes = await http.post(PRESIGN_UPLOAD_URL, {
				filename,
				contentType: file.type,
				contentLength: file.size
			});
			if (!putRes.ok) {
				errorMsg = `프로필 업로드 준비 실패 (${putRes.status})`;
				uploading = false;
				return;
			}
			const envPut = (await putRes.json()) as ApiEnvelope<PresignPutResult>;
			const putUrl = envPut?.result?.url;
			const headers = envPut?.result?.headers ?? {};
			const key = envPut?.result?.key;
			if (!putUrl || !key) {
				errorMsg = 'presign 데이터 이상';
				uploading = false;
				return;
			}

			// 2) S3 업로드
			await xhrPutWithProgress(putUrl, file, headers);

			// 3) confirm
			const confirmRes = await http.post(PRESIGN_CONFIRM_URL, { newKey: key });
			if (!confirmRes.ok) {
				errorMsg = `프로필 갱신 실패 (${confirmRes.status})`;
				uploading = false;
				return;
			}

			// 4) 캐시 비우고 새 URL
			clearAvatarCache();
			await renewAvatar();
			bumpAvatar();
			openSuccess('프로필 이미지가 변경되었습니다.');
		} catch (e) {
			errorMsg = '이미지 업로드 실패';
		} finally {
			uploading = false;
			file = null;
		}
	}

	// presigned URL 만료 시 1회 재발급
	let renewing = false;
	async function onImgError() {
		if (renewing) return;
		renewing = true;
		await renewAvatar();
		renewing = false;
	}
</script>

<!-- 컨테이너 카드 -->
<div class="mx-auto max-w-2xl p-4 sm:p-6">
	<div class="rounded-2xl border border-gray-100 bg-white p-6 shadow-md sm:p-10">
		<!-- 아바타 + 편집 버튼 + 인사말 -->
		<div class="flex flex-col items-center">
			<div class="relative">
				<img
					src={avatarUrl ?? DEFAULT_AVATAR}
					alt="프로필"
					class="h-32 w-32 rounded-full object-cover ring-2 ring-gray-200"
					on:error={onImgError}
				/>
				<input id="avatarFile" type="file" accept="image/*" class="hidden" on:change={onPick} />
				<button
					class="absolute -right-1 -bottom-1 flex h-9 w-9 items-center justify-center rounded-full bg-black text-white shadow
                 hover:bg-gray-800 focus:ring-2 focus:ring-black/30 focus:outline-none"
					on:click={() => document.getElementById('avatarFile')?.click()}
					aria-label="프로필 이미지 변경"
				>
					<svg
						xmlns="http://www.w3.org/2000/svg"
						class="h-4 w-4"
						viewBox="0 0 24 24"
						fill="none"
						stroke="currentColor"
						stroke-width="2"
					>
						<path d="M12 20h9" />
						<path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4 12.5-12.5z" />
					</svg>
				</button>
			</div>

			<h2 class="mt-5 text-xl font-bold text-gray-900">
				{username || '회원'}님 환영해요.
			</h2>

			{#if uploading}
				<div class="mt-3 w-56 text-xs text-gray-600">이미지 업로드 중… {uploadProgress}%</div>
			{/if}
		</div>

		<!-- 폼 -->
		<div class="mt-8 space-y-5">
			<div>
				<label class="mb-1 block text-sm font-medium text-gray-700">회원이름</label>
				<input
					class="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2 text-gray-700"
					value={username}
					readonly
				/>
			</div>

			<div>
				<label class="mb-1 block text-sm font-medium text-gray-700">이메일</label>
				<input
					class="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2 text-gray-700"
					value={email}
					readonly
				/>
			</div>

			<div>
				<label for="nickname" class="mb-1 block text-sm font-medium text-gray-700">
					닉네임 <span class="text-red-500">*</span>
				</label>
				<input
					id="nickname"
					class="w-full rounded-lg border px-3 py-2 transition outline-none
                 {invalid && trimmed !== currentNickname
						? 'border-red-300 focus:border-red-400 focus:ring-1 focus:ring-red-300'
						: 'border-gray-300 focus:border-gray-400 focus:ring-1 focus:ring-gray-200'}"
					bind:value={nickname}
					maxlength={nicknameRule.max}
					placeholder="최대 20자, 영문/숫자/한글/밑줄/공백"
				/>
				<div class="mt-1 flex justify-between text-xs">
					<span class={invalid && trimmed !== currentNickname ? 'text-red-600' : 'text-gray-500'}>
						{#if !validLen}
							길이는 최대 {nicknameRule.max}자여야 합니다.
						{:else if !validRegex}
							영문/숫자/한글/밑줄(_)과 공백만 사용할 수 있습니다.
						{:else if !dirty}
							현재 닉네임과 동일합니다.
						{:else}
							사용 가능한 닉네임입니다.
						{/if}
					</span>
					<span class="text-gray-400">{trimmed.length} / {nicknameRule.max}</span>
				</div>
			</div>

			{#if errorMsg}
				<div class="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
					{errorMsg}
				</div>
			{/if}

			<button
				class="mt-6 w-full rounded-xl bg-black px-4 py-3 text-center font-semibold text-white disabled:opacity-40"
				on:click={saveNickname}
				disabled={!canSubmit}
			>
				저장하기
			</button>
		</div>
	</div>
</div>
{#if successOpen}
  <div class="fixed inset-0 z-[100] flex items-center justify-center">
    <!-- 오버레이 -->
    <div class="absolute inset-0 bg-black/40" on:click={closeSuccess}></div>

    <!-- 모달 -->
    <div class="relative z-[110] w-full max-w-sm rounded-2xl bg-white p-6 shadow-2xl
                animate-[fadeIn_.15s_ease-out]">
      <!-- 아이콘 원 -->
      <div class="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-emerald-100">
        <!-- 체크 아이콘 -->
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-emerald-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M20 6 9 17l-5-5"/>
        </svg>
      </div>

      <h3 class="mt-4 text-center text-lg font-semibold text-gray-900">{successTitle}</h3>
      <div class="mt-6">
        <button
          class="w-full rounded-xl bg-black px-4 py-2.5 text-white font-medium hover:bg-gray-900 focus:outline-none focus:ring-2 focus:ring-black/30"
          on:click={() => { successAction?.(); closeSuccess(); }}
        >
          확인
        </button>
      </div>
    </div>
  </div>
{/if}

<style>
  @keyframes fadeIn {
    from { transform: translateY(6px); opacity: 0 }
    to   { transform: translateY(0);   opacity: 1 }
  }
</style>