<script lang="ts">
	import { goto } from '$app/navigation';
	import { isLoggedIn } from '$lib/stores/auth';
	import { fetchWithAuth } from '$lib/api/fetchWithAuth';
	import { onDestroy, onMount } from 'svelte';

	export let handleLogin: () => void;
	export let handleLogout: () => Promise<void>;

	// 이동 버튼
	function goToCreatePage() {
		goto('/studies/new');
	}
	function goToMyApplications() {
		menuOpen = false;
		goto('/mypage/applications');
	}

	// --- 타입들 ---
	type ApiEnvelope<T> = { isSuccess: boolean; code: string; message: string; result: T };
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
	type PresignGetResult = { method: 'GET'; url: string; key: string; expiresAt: string };

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
		const res = await fetchWithAuth('/api/s3/presign/me', { method: 'GET' });
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
		const unsub = isLoggedIn.subscribe((v) => {
			if (v) void loadAvatar();
			else {
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

	function toggleMenu() {
		menuOpen = !menuOpen;
	}
	function openModal() {
		modalOpen = true;
		menuOpen = false;
	}
	function closeModal() {
		modalOpen = false;
		resetUploadState();
	}

	// --- 업로드 로직 ---
	let file: File | null = null;
	let uploading = false;
	let progress = 0;
	let message = '';
	let etag: string | null = null;
	let previewUrl: string | null = null;

	function resetUploadState() {
		file = null;
		uploading = false;
		progress = 0;
		message = '';
		etag = null;
		previewUrl = null;
	}

	function onPick(e: Event) {
		const input = e.target as HTMLInputElement;
		file = input.files?.[0] ?? null;
		progress = 0;
		message = '';
		etag = null;
		previewUrl = null;
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

	// 금지 헤더(Host 등) 제외하고 필요한 헤더만 설정
	function xhrPutWithProgress(
		url: string,
		file: File,
		headers: Record<string, string>
	): Promise<string | null> {
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
	}

	async function uploadAvatar() {
		if (!file) {
			message = '파일을 선택하세요.';
			return;
		}
		uploading = true;
		message = '프리사인 발급 중…';

		const filename = `${crypto.randomUUID()}_avatar.${extFromType(file.type)}`;

		// 1) presign PUT
		const putRes = await fetchWithAuth('/api/s3/presign/upload', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({ filename, contentType: file.type, contentLength: file.size })
		});
		if (!putRes.ok) {
			uploading = false;
			message = `presign 실패: ${putRes.status}`;
			return;
		}
		const envPut: ApiEnvelope<PresignPutResult> = await putRes.json();
		const putUrl = envPut?.result?.url;
		const headers = envPut?.result?.headers ?? {};
		const key = envPut?.result?.key;
		if (!putUrl || !/^https?:\/\//.test(putUrl) || !key) {
			uploading = false;
			message = 'presign 데이터 이상';
			return;
		}

		// 2) 브라우저가 S3로 직접 업로드
		try {
			message = 'S3 업로드 중…';
			await xhrPutWithProgress(putUrl, file, headers);
		} catch (e: any) {
			uploading = false;
			message = `업로드 실패: ${e?.message ?? e}`;
			return;
		}

		// 3) 서버에 업로드 성공 확정(confirm)
		message = '업로드 확인 중…';
		const confirmRes = await fetchWithAuth('/api/s3/presign/confirm', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({ newKey: key })
		});
		if (!confirmRes.ok) {
			uploading = false;
			message = `업로드는 됐지만 프로필 갱신 실패: ${confirmRes.status}`;
			return;
		}

		// 4) 로컬 캐시 비우고 새 presigned GET 재발급
		clearAvatarCache();
		const ok = await renewAvatar();
		message = ok ? '완료!' : '새 URL 갱신 실패.';
		uploading = false;
		closeModal();
	}
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
					<!-- 아바타 & 드롭다운 -->
					<div class="relative" data-avatar>
						{#if avatarUrl}
							<img
								src={avatarUrl}
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
						{:else}
							<div
								class="size-8.5 cursor-pointer rounded-full bg-gray-300"
								style="aspect-ratio: 1 / 1;"
								aria-label="no avatar"
								on:click={toggleMenu}
								aria-haspopup="menu"
								aria-expanded={menuOpen}
							/>
						{/if}

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
										on:click={openModal}
									>
										프로필 편집
									</button>
									<button
										class="w-full px-3 py-2 text-left text-sm text-red-600 hover:bg-red-50"
										on:click={() => {
											menuOpen = false;
											void handleLogout();
										}}
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

	<!-- 업로드 모달 -->
	{#if modalOpen}
		<div class="fixed inset-0 z-30 flex items-center justify-center">
			<div class="absolute inset-0 bg-black/30" on:click={() => (modalOpen = false)}></div>

			<div class="relative z-40 w-full max-w-md rounded-xl bg-white p-5 shadow-xl">
				<h3 class="mb-3 text-lg font-semibold">프로필 이미지 업로드</h3>

				<div class="space-y-3">
					<!-- 실제 input은 숨기고 버튼으로만 트리거 -->
					<input id="avatarFile" type="file" accept="image/*" class="sr-only" on:change={onPick} />

					<!-- 테두리 상자 + 파란 버튼 + 상태 텍스트 -->
					<div class="rounded-lg border border-gray-200 p-4">
						<div class="flex items-center gap-3">
							<button
								type="button"
								class="inline-flex items-center gap-2 rounded-md bg-blue-600 px-3 py-1.5 text-white
                       hover:bg-blue-700 focus:ring-2 focus:ring-blue-400 focus:outline-none disabled:opacity-50"
								on:click={() => document.getElementById('avatarFile')?.click()}
								disabled={uploading}
							>
								파일 선택
							</button>

							{#if file}
								<span class="max-w-[14rem] truncate text-sm text-gray-800">{file.name}</span>
								<span class="text-xs text-gray-500">{file.type || '파일'}</span>
							{:else}
								<span class="text-sm text-gray-500">선택된 파일 없음</span>
							{/if}
						</div>
					</div>

					<!-- 액션 버튼 -->
					<div class="flex items-center justify-end gap-2">
						<button
							class="rounded bg-gray-100 px-4 py-1 text-sm"
							on:click={() => (modalOpen = false)}>취소</button
						>
						<button
							class="rounded bg-blue-600 px-4 py-1 text-sm text-white disabled:opacity-50"
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
	:global(.shadow-lg) {
		box-shadow:
			0 10px 20px rgba(0, 0, 0, 0.08),
			0 6px 6px rgba(0, 0, 0, 0.06);
	}
</style>
