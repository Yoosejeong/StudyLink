<script lang="ts">
  import { onDestroy } from 'svelte';
  import { page } from '$app/stores';
  import { goto } from '$app/navigation';
  import { User } from 'lucide-svelte';

  interface Study {
    studyPostId: number;
    title: string;
    nickname: string;
    maxPeople: number;
    acceptedPeople: number;
    studyStatus: 'RECRUITING' | 'CLOSED';
    updatedAt: string;
  }

  let studies: Study[] = [];
  let totalPages = 1;
  let currentPage = 0;
  let currentStatus: string | null = null;   // 'RECRUITING' | null
  let keyword = '';                           // 검색어(입력값)

  const baseUrl = import.meta.env.VITE_API_BASE_URL;

  const unsubscribe = page.subscribe(async ($page) => {
    // URL 쿼리 → 로컬 상태 동기화
    const pageParam   = Number($page.url.searchParams.get('page') ?? '0');
    const statusParam = $page.url.searchParams.get('status');
    const kwParam     = $page.url.searchParams.get('rawKeyword') ?? '';

    currentPage   = pageParam;
    currentStatus = statusParam;
    keyword       = kwParam;   // 입력창에도 반영

    try {
      const url = new URL(`${baseUrl}/api/study-posts`);
      url.searchParams.set('page', String(pageParam));
      if (statusParam) url.searchParams.set('status', statusParam);

      const trimmed = kwParam.trim();
      if (trimmed.length >= 2) {
        url.searchParams.set('rawKeyword', trimmed);   // 2~50자만 서버에 전달
      }
      const res  = await fetch(url.toString());
      const data = await res.json();

      studies    = data.result?.content ?? [];
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
    params.set('page', '0');                          // 검색 시 첫 페이지로
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

<main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12"> 
    <!-- ✅ 히어로 섹션 -->
  <div class="bg-gradient-to-r from-yellow-300 to-yellow-400 rounded-3xl mx-4 mb-16 overflow-hidden">
    <div class="relative px-8 py-12 md:px-16 md:py-16">
      <div class="flex flex-col lg:flex-row items-center justify-between">
        <div class="flex-1 mb-8 lg:mb-0">
          <div class="inline-block bg-pink-400 text-white px-4 py-2 rounded-full text-sm font-medium mb-4">
            JOIN US
          </div>
          <h2 class="text-4xl md:text-5xl font-bold text-gray-900 mb-4">Grow Together! 🤍</h2>
          <p class="text-lg md:text-xl text-gray-700 max-w-lg">원하는 스터디에 지원하고, 함께 성장하세요!</p>
        </div>
        <div class="flex-1 relative max-w-md">
          <div class="absolute -top-4 -right-4 bg-white rounded-full p-3 shadow-lg">
            <div class="w-8 h-8 bg-purple-500 rounded-full flex items-center justify-center">📢</div>
          </div>
          <div class="space-y-4">
            <div class="bg-white rounded-2xl p-4 shadow-md ml-8">
              <p class="font-medium text-gray-800">스터디를 모집하고 지원해요! 📚</p>
            </div>
            <div class="bg-white rounded-2xl p-4 shadow-md mr-8">
              <p class="text-gray-700">StudyLink에서</p>
              <p class="text-gray-700">함께 성장할 팀원을 찾아보세요 🚀</p>
            </div>
            <div class="bg-green-400 text-white rounded-2xl p-4 shadow-md ml-4">
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
      class={`flex items-center gap-2 px-6 py-3 rounded-full text-sm font-medium transition-all border-2
        ${!currentStatus
          ? 'bg-gray-100 border-gray-200 text-gray-800'
          : 'bg-white border-gray-200 text-gray-600 hover:bg-gray-50'}`}
    >
      전체보기
    </button>

    <button
      on:click={() => filterByStatus('RECRUITING')}
      class={`flex items-center gap-2 px-6 py-3 rounded-full text-sm font-medium transition-all border-2
        ${currentStatus === 'RECRUITING'
          ? 'bg-teal-50 border-teal-300 text-teal-700'
          : 'bg-white border-teal-200 text-teal-600 hover:bg-teal-50'}`}
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
      class="w-56 sm:w-64 md:w-72 rounded-full border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400"
    />
    {#if keyword.trim().length > 0}
      <button
        class="px-3 py-2 text-sm rounded-full border border-gray-300 bg-white hover:bg-gray-50"
        on:click={clearSearch}
        aria-label="검색어 지우기"
      >지우기</button>
    {/if}
    <button
      class="px-3 py-2 text-sm rounded-full bg-gray-600 text-white hover:bg-blue-700 disabled:bg-gray-300"
      on:click={doSearch}
      disabled={keyword.trim().length > 0 && keyword.trim().length < 2}
    >검색</button>
  </div>
</div>

  <!-- 목록 -->
  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
    {#if studies.length === 0}
      <div class="col-span-full text-center text-gray-500 py-12">
        검색 결과가 없어요.
      </div>
    {:else}
      {#each studies as study (study.studyPostId)}
        <button
          type="button"
          class="w-full text-left bg-white rounded-xl shadow-md p-4 hover:shadow-lg transition cursor-pointer"
          on:click={() => goToDetail(study.studyPostId)}
        >
          <div class="mb-3">
            <span
              class="inline-block px-2 py-1 rounded text-xs"
              class:bg-yellow-100={study.studyStatus === 'RECRUITING'}
              class:text-yellow-800={study.studyStatus === 'RECRUITING'}
              class:bg-gray-300={study.studyStatus === 'CLOSED'}
              class:text-gray-800={study.studyStatus === 'CLOSED'}
            >
              {study.studyStatus}
            </span>
          </div>
          <h3 class="font-semibold text-gray-900 mb-2 line-clamp-2 leading-tight">
            {study.title}
          </h3>
          <div class="flex items-center mb-1 text-sm text-gray-600">
            <User class="w-4 h-4 text-blue-600" />
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
  {#each Array(totalPages).fill(0).map((_, i) => i) as page}
    <button
      on:click={() => goToPage(page)}
      class={`px-4 py-2 border rounded transition
        ${page === currentPage
          ? 'border-gray-700 bg-gray-700 text-white opacity-95'
          : 'border-gray-400 bg-gray-300 text-gray-900 opacity-95 hover:bg-gray-400'}`}
    >
      {page + 1}
    </button>
  {/each}
</div>
</main> 