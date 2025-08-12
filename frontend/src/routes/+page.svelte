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
  let currentStatus: string | null = null;  // 상태 필터 ('RECRUITING' or null)

  const baseUrl = import.meta.env.VITE_API_BASE_URL;

  const unsubscribe = page.subscribe(async ($page) => {
    const pageParam = Number($page.url.searchParams.get('page') ?? '0');
    currentPage = pageParam;

    const statusParam = $page.url.searchParams.get('status');
    currentStatus = statusParam;

    try {
      // status 쿼리 파라미터가 있을 때만 붙임
      const url = new URL(`${baseUrl}/api/study-posts`);
      url.searchParams.set('page', String(pageParam));
      if (statusParam) {
        url.searchParams.set('status', statusParam);
      }
      const res = await fetch(url.toString());
      const data = await res.json();

      studies = data.result?.content ?? [];
      totalPages = data.result?.totalPages ?? 1;
    } catch (err) {
      console.error('❌ 목록 불러오기 실패', err);
    }
  });

  onDestroy(() => {
    unsubscribe();
  });

  function goToDetail(id: number) {
    goto(`/studies/${id}`);
  }

  // 페이지 이동 시 현재 상태 필터 유지해서 쿼리 업데이트
  function goToPage(page: number) {
    const params = new URLSearchParams();
    params.set('page', String(page));
    if (currentStatus) {
      params.set('status', currentStatus);
    }
    goto(`/?${params.toString()}`);
  }

  // 상태 필터 버튼 클릭 핸들러
  function filterByStatus(status: string | null) {
    const params = new URLSearchParams();
    params.set('page', '0');  // 필터 변경 시 첫 페이지로 이동
    if (status) {
      params.set('status', status);
    }
    goto(`/?${params.toString()}`);
  }
</script>


<main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
  <!-- 히어로 -->
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

<div class="mb-6 flex gap-4 justify-start">
  <!-- 전체보기 -->
  <button
    on:click={() => filterByStatus(null)}
    class={`flex items-center gap-2 px-6 py-3 rounded-full text-sm font-medium transition-all border-2
      ${!currentStatus
        ? 'bg-gray-100 border-gray-200 text-gray-800'
        : 'bg-white border-gray-200 text-gray-600 hover:bg-gray-50'}`}
  >
    전체보기
  </button>

  <!-- 모집중만 보기 -->
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


  <!-- ✅ 스터디 목록 카드 -->
  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
  {#each studies as study (study.studyPostId)}
    <button
      type="button"
      class="w-full text-left border rounded-lg p-4 hover:shadow-lg transition-shadow cursor-pointer"
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
</div>


  <!-- ✅ 페이지버튼 -->
  <div class="mt-10 flex justify-center gap-2">
  {#each Array(totalPages).fill(0).map((_, i) => i) as page}
    <button
      on:click={() => goToPage(page)}
      class="px-4 py-2 border rounded"
      class:bg-blue-500={page === currentPage}
      class:text-white={page === currentPage}
      class:text-gray-800={page !== currentPage}
    >
      {page + 1}
    </button>
  {/each}
</div>
</main>
