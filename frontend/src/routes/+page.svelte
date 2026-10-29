<script lang="ts">
	import { onMount } from 'svelte';
	import { page } from '$app/stores';
	import { goto, afterNavigate } from '$app/navigation';
	import { Search } from 'lucide-svelte';

	type CategoryCode =
		| 'BACKEND'
		| 'FRONTEND'
		| 'MOBILE'
		| 'DATA_AI'
		| 'CLOUD_DEVOPS'
		| 'ALGORITHM_CS'
		| 'GAME'
		| 'CERT';

	const CAT_LABEL: Record<CategoryCode, string> = {
		BACKEND: '백엔드',
		FRONTEND: '프론트엔드',
		MOBILE: '모바일',
		DATA_AI: '데이터·AI',
		CLOUD_DEVOPS: '클라우드·DevOps',
		ALGORITHM_CS: '알고리즘·CS',
		GAME: '게임·그래픽',
		CERT: '자격증'
	};

	interface Study {
		studyPostId: number;
		title: string;
		nickname: string;
		maxPeople: number;
		acceptedPeople: number;
		category: CategoryCode;
		studyStatus: 'RECRUITING' | 'CLOSED';
		profileKey?: string | null; // S3 key
		profileUrl?: string | null;
		tags: string[];
		updatedAt: string;
	}

	let studies: Study[] = [];
	let hasNext = false;
	let nextCursorCreatedAt: string | null = null;
	let currentStatus: string | null = null; // 'RECRUITING' | null
	let nextCursorId: number | null = null;
	let keyword = ''; // 검색어(입력값)
	let activeKeyword = ''; // 현재 로드된 검색어
	let pendingRestoreScrollY: number | null = null;
	const LIST_SCROLL_STORAGE_KEY = 'study-list-scroll-y';
	const LIST_STATE_STORAGE_KEY = 'study-list-state';

	const isPageReload = (() => {
		if (typeof window === 'undefined') return false;
		const nav = performance.getEntriesByType('navigation')[0] as
			| PerformanceNavigationTiming
			| undefined;
		return nav?.type === 'reload';
	})();

	if (typeof window !== 'undefined' && isPageReload) {
		sessionStorage.removeItem(LIST_STATE_STORAGE_KEY);
		sessionStorage.removeItem(LIST_SCROLL_STORAGE_KEY);
	}

	export const snapshot = {
		capture: () => ({
			studies,
			hasNext,
			nextCursorCreatedAt,
			nextCursorId,
			currentStatus,
			keyword,
			activeKeyword,
			scrollY: typeof window !== 'undefined' ? window.scrollY : 0
		}),
		restore: (value) => {
			studies = value.studies;
			hasNext = value.hasNext;
			nextCursorCreatedAt = value.nextCursorCreatedAt;
			nextCursorId = value.nextCursorId;
			currentStatus = value.currentStatus;
			keyword = value.keyword;
			activeKeyword = value.activeKeyword ?? value.keyword;
			pendingRestoreScrollY = value.scrollY ?? 0;
		}
	};

	type ListState = {
		studies: Study[];
		hasNext: boolean;
		nextCursorCreatedAt: string | null;
		nextCursorId: number | null;
		currentStatus: string | null;
		keyword: string;
		activeKeyword: string;
		scrollY: number;
	};

	function saveListState() {
		if (typeof window === 'undefined') return;
		const value: ListState = {
			studies,
			hasNext,
			nextCursorCreatedAt,
			nextCursorId,
			currentStatus,
			keyword,
			activeKeyword,
			scrollY: window.scrollY
		};
		sessionStorage.setItem(LIST_STATE_STORAGE_KEY, JSON.stringify(value));
	}

	function restoreListState() {
		if (typeof window === 'undefined') return false;
		const raw = sessionStorage.getItem(LIST_STATE_STORAGE_KEY);
		if (!raw) return false;
		try {
			const value = JSON.parse(raw) as ListState;
			studies = value.studies ?? [];
			hasNext = value.hasNext ?? false;
			nextCursorCreatedAt = value.nextCursorCreatedAt ?? null;
			nextCursorId = value.nextCursorId ?? null;
			currentStatus = value.currentStatus ?? null;
			keyword = value.keyword ?? '';
			activeKeyword = value.activeKeyword ?? value.keyword ?? '';
			pendingRestoreScrollY = value.scrollY ?? 0;
			sessionStorage.removeItem(LIST_STATE_STORAGE_KEY);
			return true;
		} catch {
			sessionStorage.removeItem(LIST_STATE_STORAGE_KEY);
			return false;
		}
	}

	// ✅ 기본 이미지 경로
	const DEFAULT_AVATAR = '/avatars/default.jpg';

	// ✅ 이미지 src 선택 (공개 URL 우선)
	function avatarSrc(s: Study) {
		return s.profileUrl || undefined;
	}

	// ✅ 이미지 로딩 실패 시 폴백
	function onImgError(e: Event) {
		const img = e.target as HTMLImageElement;
		// 이미 기본 아바타면 더 바꾸지 않음(무한 onerror 방지)
		if (!img.src.endsWith(DEFAULT_AVATAR)) {
			img.src = DEFAULT_AVATAR;
		}
	}

	const baseUrl = import.meta.env.VITE_API_BASE_URL;

	async function loadList(reset = false) {
		if (reset) {
			studies = [];
			nextCursorCreatedAt = null;
			nextCursorId = null;
			hasNext = false;
		}

		try {
			const url = new URL(`${baseUrl}/api/study-posts`);
			if (currentStatus) url.searchParams.set('status', currentStatus);

			const trimmed = activeKeyword.trim();
			if (trimmed.length >= 2) {
				url.searchParams.set('rawKeyword', trimmed);
			}

			if (!reset && nextCursorCreatedAt && nextCursorId !== null) {
				url.searchParams.set('lastCreatedAt', nextCursorCreatedAt);
				url.searchParams.set('lastId', String(nextCursorId));
			}

			const res = await fetch(url.toString());
			const data = await res.json();

			if (data.isSuccess) {
				const fetchedItems = data.result?.items ?? [];
				if (reset) {
					studies = fetchedItems;
				} else {
					studies = [...studies, ...fetchedItems];
				}
				hasNext = data.result?.hasNext ?? false;
				nextCursorCreatedAt = data.result?.nextCursorCreatedAt ?? null;
				nextCursorId = data.result?.nextCursorId ?? null;
			}
		} catch (err) {
			console.error('❌ 목록 불러오기 실패', err);
		}
	}

	onMount(() => {
		if (studies.length === 0) {
			// 뒤로가기 복원 대기 상태면 초기 API 호출을 건너뛴다.
			// (afterNavigate(popstate)에서 sessionStorage 상태를 복원)
			if (typeof window !== 'undefined' && sessionStorage.getItem(LIST_STATE_STORAGE_KEY)) {
				return;
			}

			const statusParam = $page.url.searchParams.get('status');
			const kwParam = $page.url.searchParams.get('rawKeyword') ?? '';
			currentStatus = statusParam;
			activeKeyword = kwParam;
			keyword = kwParam;
			loadList(true);
		}
	});

	afterNavigate((navigation) => {
		// 뒤로가기(popstate) 시에는 스냅샷 복구를 절대적으로 신뢰하고 API 재호출(loadList)을 완전 차단합니다!
		if (navigation && navigation.type === 'popstate') {
			restoreListState();
			let restoreY = pendingRestoreScrollY;
			if (restoreY === null && typeof window !== 'undefined') {
				const savedY = Number(sessionStorage.getItem(LIST_SCROLL_STORAGE_KEY));
				if (Number.isFinite(savedY) && savedY >= 0) {
					restoreY = savedY;
				}
			}

			if (restoreY !== null && typeof window !== 'undefined') {
				pendingRestoreScrollY = null;
				requestAnimationFrame(() => {
					requestAnimationFrame(() => {
						window.scrollTo({ top: restoreY as number, left: 0, behavior: 'auto' });
					});
				});
				sessionStorage.removeItem(LIST_SCROLL_STORAGE_KEY);
			}
			return; // 👈 팝스테이트 시 여기서 무조건 종료! 스냅샷 배열(24개) 초기화 방지
		}

		// 일반적인 페이지 이동(검색 클릭 등)일 때만 URL을 검사해서 목록을 불러옴
		const statusParam = $page.url.searchParams.get('status');
		const kwParam = $page.url.searchParams.get('rawKeyword') ?? '';

		if (currentStatus !== statusParam || activeKeyword !== kwParam) {
			currentStatus = statusParam;
			activeKeyword = kwParam;
			keyword = kwParam; // 입력창에도 반영 동기화
			loadList(true);
		}
	});

	function goToDetail(id: number) {
		saveListState();
		if (typeof window !== 'undefined') {
			sessionStorage.setItem(LIST_SCROLL_STORAGE_KEY, String(window.scrollY));
		}
		goto(`/studies/${id}`);
	}

	// 검색 실행
	function doSearch() {
		const params = new URLSearchParams();
		if (currentStatus) params.set('status', currentStatus);

		const trimmed = keyword.trim();
		if (trimmed.length >= 2) params.set('rawKeyword', trimmed); // 2자 미만이면 전송 안 함
		goto(`/?${params.toString()}`);
	}

	// Enter로 검색
	function onSearchKeydown(e: KeyboardEvent) {
		if (e.key === 'Enter') {
			e.preventDefault();
			doSearch();
		}
	}

	// 검색어 초기화
	function clearSearch() {
		keyword = '';
		const params = new URLSearchParams();
		if (currentStatus) params.set('status', currentStatus);
		goto(`/?${params.toString()}`);
	}

	// 더보기
	function loadMore() {
		if (hasNext) {
			loadList(false);
		}
	}

	// 상태 필터 변경 (검색 유지)
	function filterByStatus(status: string | null) {
		const params = new URLSearchParams();
		if (status) params.set('status', status);
		const trimmed = keyword.trim();
		if (trimmed.length >= 2) params.set('rawKeyword', trimmed);
		goto(`/?${params.toString()}`);
	}

	function toggleRecruitingFilter(e: Event) {
		const checked = (e.currentTarget as HTMLInputElement).checked;
		filterByStatus(checked ? 'RECRUITING' : null);
	}
</script>

<main class="mx-auto max-w-7xl px-4 py-8 sm:px-6 sm:py-10 lg:px-8">
	<!-- 검색 바 -->
	<div
		class="flex w-full items-center gap-3 rounded-[10px] border border-[#E2E5E9] bg-white px-4 py-3 sm:px-5"
	>
		<button
			type="button"
			on:click={doSearch}
			disabled={keyword.trim().length > 0 && keyword.trim().length < 2}
			aria-label="검색"
			class="shrink-0 text-gray-500 hover:text-[#252A32] disabled:cursor-not-allowed disabled:text-gray-300"
		>
			<Search size={20} aria-hidden="true" />
		</button>
		<input
			type="text"
			bind:value={keyword}
			on:keydown={onSearchKeydown}
			placeholder="찾고 싶은 스터디를 검색하세요"
			aria-label="스터디 검색"
			class="min-w-0 flex-1 border-0 bg-transparent py-1 text-[#252A32] shadow-none ring-0 placeholder:text-gray-500 focus:border-0 focus:ring-0 focus:outline-none"
		/>
		{#if keyword.trim().length > 0}
			<button
				class="shrink-0 px-1 py-2 text-sm text-gray-500 hover:text-[#252A32]"
				on:click={clearSearch}
				aria-label="검색어 지우기">지우기</button
			>
		{/if}
	</div>

	<!-- 목록 제목 + 상태 필터 -->
	<div class="mt-8 mb-6 flex items-center justify-between gap-4">
		<h2 class="text-2xl font-bold text-[#252A32]">스터디 모집글</h2>
		<label class="flex shrink-0 cursor-pointer items-center gap-2 text-sm text-gray-600">
			<input
				type="checkbox"
				checked={currentStatus === 'RECRUITING'}
				on:change={toggleRecruitingFilter}
				class="size-4 rounded border-gray-300 accent-[#252A32]"
			/>
			<span>모집 중만 보기</span>
		</label>
	</div>

	<!-- 목록 -->
	<div class="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
		{#if studies.length === 0}
			<div class="col-span-full py-12 text-center text-gray-500">검색 결과가 없어요.</div>
		{:else}
			{#each studies as study (study.studyPostId)}
				<button
					type="button"
					class="w-full cursor-pointer rounded-xl bg-white p-4 text-left shadow-md transition hover:shadow-lg"
					on:click={() => goToDetail(study.studyPostId)}
				>
					<div class="mb-3">
						<span
							class="inline-block rounded px-2 py-1 text-xs"
							class:bg-yellow-100={study.studyStatus === 'RECRUITING'}
							class:text-yellow-800={study.studyStatus === 'RECRUITING'}
							class:bg-gray-300={study.studyStatus === 'CLOSED'}
							class:text-gray-800={study.studyStatus === 'CLOSED'}
						>
							{study.studyStatus}
						</span>

						<!-- ✅ 카테고리 뱃지(오른쪽) -->
						<span class="ml-2 inline-block rounded bg-indigo-100 px-2 py-1 text-xs text-indigo-800">
							{CAT_LABEL[study.category] ?? study.category}
						</span>
					</div>
					<h3 class="mb-2 line-clamp-2 leading-tight font-semibold text-gray-900">
						{study.title}
					</h3>

					<!-- ✅ 태그 뱃지 -->
					{#if study.tags?.length}
						<div class="mb-2 flex flex-wrap gap-2">
							{#each study.tags.slice(0, 3) as tag}
								<span
									class="rounded-full bg-gray-100 px-2.5 py-0.5 text-xs font-medium text-gray-600"
								>
									#{tag}
								</span>
							{/each}
							{#if study.tags.length > 3}
								<span
									class="rounded-full bg-gray-100 px-2.5 py-0.5 text-xs font-medium text-gray-600"
								>
									+{study.tags.length - 3}
								</span>
							{/if}
						</div>
					{/if}
					<!-- ✅ 프로필 아바타 + 닉네임 -->
					<div class="mb-1 flex items-center gap-2 text-sm text-gray-600">
						<img
							src={study.profileUrl ?? DEFAULT_AVATAR}
							alt="프로필"
							class="h-5 w-5 rounded-full object-cover ring-1 ring-gray-200"
							loading="lazy"
							decoding="async"
							on:error={onImgError}
						/>
						<span class="font-medium">{study.nickname}</span>
					</div>
					<div class="text-sm text-gray-500">
						인원 {study.acceptedPeople} / {study.maxPeople}
					</div>
				</button>
			{/each}
		{/if}
	</div>

	<!-- ✅ 더보기 버튼 -->
	{#if hasNext}
		<div class="mt-10 flex justify-center">
			<button
				on:click={loadMore}
				class="rounded-full border-2 border-gray-200 bg-white px-8 py-3 text-sm font-medium text-gray-700 transition hover:bg-gray-50 focus:ring-2 focus:ring-gray-200 focus:outline-none"
			>
				더보기
			</button>
		</div>
	{/if}
</main>
