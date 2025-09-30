<script lang="ts">
	import { onDestroy } from 'svelte';
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import { User } from 'lucide-svelte';

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
		tags: string[];
		updatedAt: string;
	}

	let studies: Study[] = [];
	let totalPages = 1;
	let currentPage = 0;
	let currentStatus: string | null = null; // 'RECRUITING' | null
	let keyword = ''; // 검색어(입력값)

	const baseUrl = import.meta.env.VITE_API_BASE_URL;

	const unsubscribe = page.subscribe(async ($page) => {
		// URL 쿼리 → 로컬 상태 동기화
		const pageParam = Number($page.url.searchParams.get('page') ?? '0');
		const statusParam = $page.url.searchParams.get('status');
		const kwParam = $page.url.searchParams.get('rawKeyword') ?? '';

		currentPage = pageParam;
		currentStatus = statusParam;
		keyword = kwParam; // 입력창에도 반영

		try {
			const url = new URL(`${baseUrl}/api/study-posts`);
			url.searchParams.set('page', String(pageParam));
			if (statusParam) url.searchParams.set('status', statusParam);

			const trimmed = kwParam.trim();
			if (trimmed.length >= 2) {
				url.searchParams.set('rawKeyword', trimmed); // 2~50자만 서버에 전달
			}
			const res = await fetch(url.toString());
			const data = await res.json();

			studies = data.result?.content ?? [];
			totalPages = data.result?.totalPages ?? 1;
		} catch (err) {
			console.error('❌ 목록 불러오기 실패', err);
		}
	});

	onDestroy(() => unsubscribe());

	function goToDetail(id: number) {
		goto(`/studies/${id}`);
	}

	// 검색 실행
	function doSearch() {
		const params = new URLSearchParams();
		params.set('page', '0'); // 검색 시 첫 페이지로
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
		params.set('page', '0');
		if (currentStatus) params.set('status', currentStatus);
		goto(`/?${params.toString()}`);
	}

	// 페이지 이동 (검색/상태 유지)
	function goToPage(page: number) {
		const params = new URLSearchParams();
		params.set('page', String(page));
		if (currentStatus) params.set('status', currentStatus);
		const trimmed = keyword.trim();
		if (trimmed.length >= 2) params.set('rawKeyword', trimmed);
		goto(`/?${params.toString()}`);
	}

	// 상태 필터 변경 (검색 유지)
	function filterByStatus(status: string | null) {
		const params = new URLSearchParams();
		params.set('page', '0');
		if (status) params.set('status', status);
		const trimmed = keyword.trim();
		if (trimmed.length >= 2) params.set('rawKeyword', trimmed);
		goto(`/?${params.toString()}`);
	}
</script>

<main class="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
	<!-- ✅ 히어로 섹션 -->
	<div
		class="mx-4 mb-16 overflow-hidden rounded-3xl bg-gradient-to-r from-yellow-300 to-yellow-400"
	>
		<div class="relative px-8 py-12 md:px-16 md:py-16">
			<div class="flex flex-col items-center justify-between lg:flex-row">
				<div class="mb-8 flex-1 lg:mb-0">
					<div
						class="mb-4 inline-block rounded-full bg-pink-400 px-4 py-2 text-sm font-medium text-white"
					>
						JOIN US
					</div>
					<h2 class="mb-4 text-4xl font-bold text-gray-900 md:text-5xl">Grow Together! 🤍</h2>
					<p class="max-w-lg text-lg text-gray-700 md:text-xl">
						원하는 스터디에 지원하고, 함께 성장하세요!
					</p>
				</div>
				<div class="relative max-w-md flex-1">
					<div class="absolute -top-4 -right-4 rounded-full bg-white p-3 shadow-lg">
						<div class="flex h-8 w-8 items-center justify-center rounded-full bg-purple-500">
							📢
						</div>
					</div>
					<div class="space-y-4">
						<div class="ml-8 rounded-2xl bg-white p-4 shadow-md">
							<p class="font-medium text-gray-800">스터디를 모집하고 지원해요! 📚</p>
						</div>
						<div class="mr-8 rounded-2xl bg-white p-4 shadow-md">
							<p class="text-gray-700">StudyLink에서</p>
							<p class="text-gray-700">함께 성장할 팀원을 찾아보세요 🚀</p>
						</div>
						<div class="ml-4 rounded-2xl bg-green-400 p-4 text-white shadow-md">
							<p>매일 새로운 스터디</p>
							<p>모집글을 확인하세요 💚</p>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>

	<!-- 상태 필터 + 검색 바 (한 줄 정렬) -->
	<div class="mb-6 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
		<!-- 왼쪽: 상태 필터 -->
		<div class="flex gap-3">
			<button
				on:click={() => filterByStatus(null)}
				class={`flex items-center gap-2 rounded-full border-2 px-6 py-3 text-sm font-medium transition-all
        ${
					!currentStatus
						? 'border-gray-200 bg-gray-100 text-gray-800'
						: 'border-gray-200 bg-white text-gray-600 hover:bg-gray-50'
				}`}
			>
				전체보기
			</button>

			<button
				on:click={() => filterByStatus('RECRUITING')}
				class={`flex items-center gap-2 rounded-full border-2 px-6 py-3 text-sm font-medium transition-all
        ${
					currentStatus === 'RECRUITING'
						? 'border-teal-300 bg-teal-50 text-teal-700'
						: 'border-teal-200 bg-white text-teal-600 hover:bg-teal-50'
				}`}
			>
				👀 모집중만 보기
			</button>
		</div>

		<!-- 오른쪽: 검색 바 -->
		<div class="flex items-center gap-2 sm:ml-auto">
			<input
				type="text"
				bind:value={keyword}
				on:keydown={onSearchKeydown}
				placeholder="제목으로 검색하세요.(2~50자)"
				class="w-56 rounded-full border border-gray-300 px-4 py-2 focus:ring-2 focus:ring-blue-400 focus:outline-none sm:w-64 md:w-72"
			/>
			{#if keyword.trim().length > 0}
				<button
					class="rounded-full border border-gray-300 bg-white px-3 py-2 text-sm hover:bg-gray-50"
					on:click={clearSearch}
					aria-label="검색어 지우기">지우기</button
				>
			{/if}
			<button
				class="rounded-full bg-gray-600 px-3 py-2 text-sm text-white hover:bg-blue-700 disabled:bg-gray-300"
				on:click={doSearch}
				disabled={keyword.trim().length > 0 && keyword.trim().length < 2}>검색</button
			>
		</div>
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
					<div class="mb-1 flex items-center text-sm text-gray-600">
						<User class="h-4 w-4 text-blue-600" />
						<span class="ml-1 font-medium">{study.nickname}</span>
					</div>
					<div class="text-sm text-gray-500">
						인원 {study.acceptedPeople} / {study.maxPeople}
					</div>
				</button>
			{/each}
		{/if}
	</div>

	<!-- ✅ 페이지버튼 -->
	<div class="mt-10 flex justify-center gap-2">
		{#each Array(totalPages)
			.fill(0)
			.map((_, i) => i) as page}
			<button
				on:click={() => goToPage(page)}
				class={`rounded border px-4 py-2 transition
        ${
					page === currentPage
						? 'border-gray-700 bg-gray-700 text-white opacity-95'
						: 'border-gray-400 bg-gray-300 text-gray-900 opacity-95 hover:bg-gray-400'
				}`}
			>
				{page + 1}
			</button>
		{/each}
	</div>
</main>
