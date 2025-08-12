<script lang="ts">
  import { onMount } from 'svelte';
  import { page } from '$app/stores';
  import { goto } from '$app/navigation';
  import { get } from 'svelte/store';
  import { isLoggedIn } from '$lib/stores/auth';
  import { http } from '$lib/api/http';
  import { registerOnReissueFail } from '$lib/api/fetchWithAuth';
  import { ArrowLeft, Users, UserCheck, Flag } from 'lucide-svelte';

  type StudyPost = {
    studyPostId: number;
    title: string;
    userId : number;
    nickname: string;
    content: string;
    maxPeople: number;
    acceptedPeople: number;
    studyStatus: 'RECRUITING' | 'CLOSED';
    createdAt: string;
    updatedAt: string;
  };

  type CurrentUser = {
    userId: number;
    nickname: string;
  };

  let studyPost: StudyPost | null = null;
  let currentUserId: number | null = null;
  let currentUserNickname: string | null = null;
  let isLoading = true;
  let errorMessage = '';
  $: studyPostId = $page.params.id;
  let hasApplied = false;
  let applicationStatus: string | null = null;

  registerOnReissueFail(() => {
    localStorage.removeItem('accessToken');
    isLoggedIn.set(false);
    alert('세션이 만료되었습니다. 다시 로그인해주세요.');
    goto('/auth');
  });

  onMount(async () => {
    if (!get(isLoggedIn)) {
      alert('로그인 후 이용해주세요.');
      goto('/auth');
      return;
    }

    await fetchUser();
    await fetchStudyPost();
    await checkApplicationStatus();
  });

  function handleEdit() {
  goto(`/studies/${studyPostId}/edit`);
  }

  async function fetchUser() {
    try {
      const res = await http.get('/api/users'); 
      if (res.ok) {
        const data: { result: CurrentUser } = await res.json();
        currentUserId = data.result.userId;
        currentUserNickname = data.result.nickname;
      } else {
        console.error('유저 정보 조회 실패', res.status);
      }
    } catch (err) {
      console.error('유저 정보 요청 실패', err);
    }
  }

  async function handleApply() {
    try {
      const res = await http.post(`/api/study-posts/${studyPostId}/applications`); 
      if (!res.ok) {
        const errorData = await safeJson(res);
        alert(`지원 실패: ${errorData?.message || res.status}`);
        return;
      }
      alert('지원이 완료되었습니다.');
      await checkApplicationStatus();
    } catch (err) {
      console.error('지원 요청 오류', err);
      alert('네트워크 오류로 지원에 실패했습니다.');
    }
  }

  async function checkApplicationStatus() {
    try {
      const res = await http.get(`/api/study-applications/check?studyPostId=${studyPostId}`);
      if (res.ok) {
        const data = await res.json();
        hasApplied = data.result.hasApplied;
        applicationStatus = data.result.applicationStatus;
      } else {
        console.error('지원 여부 확인 실패', res.status);
      }
    } catch (err) {
      console.error('지원 여부 요청 오류', err);
    }
  }

  async function fetchStudyPost() {
    try {
      const res = await http.get(`/api/study-posts/${studyPostId}`);
      if (!res.ok) {
        const errorData = await safeJson(res);
        errorMessage = errorData?.message || `조회 실패 (code: ${res.status})`;
        return;
      }
      const data = await res.json();
      studyPost = data.result;
    } catch (err) {
      console.error(err);
      errorMessage = '네트워크 오류 발생';
    } finally {
      isLoading = false;
    }
  }

  async function handleDelete() {
    const confirmed = confirm('정말 삭제하시겠습니까?');
    if (!confirmed) return;

    try {
      const res = await http.delete(`/api/study-posts/${studyPostId}`);
      if (!res.ok) {
        const errorData = await safeJson(res);
        alert(`삭제 실패: ${errorData?.message || res.status}`);
        return;
      }
      alert('삭제가 완료되었습니다.');
      goto('/');
    } catch (err) {
      console.error('삭제 요청 오류:', err);
      alert('네트워크 오류로 삭제에 실패했습니다.');
    }
  }

  async function handleCloseRecruitment() {
  const ok = confirm('정말 모집을 종료하시겠습니까?');
  if (!ok) return;

  try {
    const res = await http.patch(`/api/study-posts/${studyPostId}/close`);
    if (!res.ok) {
      const err = await safeJson(res);
      alert(`모집 종료 실패: ${err?.message || res.status}`);
      return;
    }
    alert('모집이 종료되었습니다.');
    // UI 갱신
    if (studyPost) studyPost.studyStatus = 'CLOSED';
  } catch (e) {
    console.error('모집 종료 요청 오류:', e);
    alert('네트워크 오류로 모집 종료에 실패했습니다.');
  }
}

  // 204 등 본문 없는 응답 대비
  async function safeJson(res: Response) {
    const text = await res.text();
    if (!text) return null;
    try { return JSON.parse(text); } catch { return null; }
  }
</script>


{#if isLoading}
  <p class="text-center mt-10 text-gray-500">불러오는 중...</p>
{:else if errorMessage}
  <p class="text-center mt-10 text-red-500">{errorMessage}</p>
{:else if studyPost}
  <div class="max-w-3xl mx-auto px-4 py-12">
    <div class="bg-white shadow-md rounded-lg p-8">
      <!-- 인라인 뒤로가기 -->
      <div class="mb-3">
        <button
          type="button"
          class="inline-flex items-center gap-1 text-gray-500 hover:text-gray-700 text-sm"
          on:click={() => goto('/')}
        >
          <ArrowLeft class="h-5 w-5" />
          <span>목록</span>
        </button>
      </div>


    <div class="bg-white shadow-md rounded-lg p-8">
      <!-- 헤더: 제목 + (작성자 전용) 버튼 -->
      <div class="mb-2 flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <h1 class="text-3xl md:text-4xl font-extrabold text-gray-900 leading-snug sm:max-w-xl">
          {studyPost.title}
        </h1>

        {#if currentUserId === studyPost.userId}
          <div class="mt-1 flex shrink-0 items-center gap-2 sm:mt-0">
            <button
              class="px-3 py-1.5 text-sm rounded-full border border-gray-300 bg-white text-gray-800 shadow-sm hover:bg-gray-50 transition"
              on:click={() => goto(`/studies/${studyPostId}/applications`)}
            >
              👥 지원자 목록
            </button>

            {#if studyPost.studyStatus === 'RECRUITING'}
              <button
                class="px-3 py-1.5 text-sm rounded-full border border-red-300 bg-white text-red-600 shadow-sm hover:bg-red-50 transition"
                on:click={handleCloseRecruitment}
              >
                🔒 모집 종료하기
              </button>
            {/if}
          </div>
        {/if}
      </div>

      <!-- 작성자 · 날짜 -->
      <div class="text-sm md:text-base text-gray-600">
        작성자 <span class="font-medium text-gray-800">{studyPost.nickname}</span>
        <span class="mx-2 text-gray-300">·</span>
        등록일 <span class="text-gray-700">{new Date(studyPost.createdAt).toLocaleString()}</span>
      </div>

      <!-- 구분선 -->
      <div class="my-6 h-px bg-gray-200"></div>

      <!-- 스터디 소개 -->
      <section class="mb-8">
        <h2 class="mb-2 text-sm font-semibold text-gray-700">스터디 소개</h2>
        <div class="whitespace-pre-line rounded-xl border border-gray-200 bg-white/70 p-5 leading-relaxed text-gray-900">
          {studyPost.content}
        </div>
      </section>

      <!-- 메타 정보 (타일) -->
      <section class="grid gap-4 sm:grid-cols-3">
        <div class="rounded-xl border border-gray-200 bg-white p-4">
          <div class="flex items-center justify-between">
            <span class="inline-flex items-center gap-2 text-sm text-gray-600">
              <Users class="h-4 w-4" /> 최대 인원
            </span>
            <span class="text-base font-semibold text-gray-900">{studyPost.maxPeople}명</span>
          </div>
        </div>

        <div class="rounded-xl border border-gray-200 bg-white p-4">
          <div class="flex items-center justify-between">
            <span class="inline-flex items-center gap-2 text-sm text-gray-600">
              <UserCheck class="h-4 w-4" /> 승인 인원
            </span>
            <span class="text-base font-semibold text-gray-900">{studyPost.acceptedPeople}명</span>
          </div>
        </div>

        <div class="rounded-xl border border-gray-200 bg-white p-4">
          <div class="flex items-center justify-between">
            <span class="inline-flex items-center gap-2 text-sm text-gray-600">
              <Flag class="h-4 w-4" /> 모집 상태
            </span>
            <span class={`rounded-full border px-2.5 py-1 text-sm font-medium
              ${studyPost.studyStatus === 'RECRUITING'
                ? 'border-emerald-200 bg-emerald-50 text-emerald-700'
                : 'border-gray-300 bg-gray-100 text-gray-700'}`}>
              {studyPost.studyStatus === 'RECRUITING' ? '모집 중' : '모집 종료'}
            </span>
          </div>
        </div>
      </section>

      <!-- 하단 액션 (목록 버튼 제거, 우측 정렬) -->
      <div class="mt-8 flex items-center justify-end">
        {#if currentUserId === studyPost.userId}
          <div class="flex gap-3">
            <button
              class="px-4 py-2 text-sm rounded-full border border-gray-300 bg-white text-gray-800 shadow-sm hover:bg-gray-50 transition"
              on:click={handleEdit}
            >
              수정하기
            </button>
            <button
              class="px-4 py-2 text-sm rounded-full border border-red-300 bg-white text-red-600 shadow-sm hover:bg-red-50 transition"
              on:click={handleDelete}
            >
              삭제하기
            </button>
          </div>
        {:else}
          {#if hasApplied}
            <div class="flex items-center gap-2 rounded-md border border-gray-200 bg-gray-50 px-4 py-3">
              {#if applicationStatus === 'ACCEPTED'}
                <span class="text-green-600">✔</span>
              {:else if applicationStatus === 'REJECTED'}
                <span class="text-red-600">✖</span>
              {:else}
                <span class="text-blue-600">⏳</span>
              {/if}
              <span class="font-medium text-gray-800">이미 지원한 스터디입니다.</span>
              <span
                class="rounded-full px-2 py-0.5 text-xs font-medium
                       {applicationStatus === 'ACCEPTED'
                         ? 'bg-green-100 text-green-700'
                         : applicationStatus === 'REJECTED'
                           ? 'bg-red-100 text-red-700'
                           : 'bg-blue-100 text-blue-700'}"
              >
                {applicationStatus === 'ACCEPTED'
                  ? '승인됨'
                  : applicationStatus === 'REJECTED'
                    ? '거절됨'
                    : '진행중'}
              </span>
            </div>
          {:else if studyPost.studyStatus === 'RECRUITING'}
            <button
              class="px-4 py-2 text-sm rounded-full border border-blue-300 bg-white text-blue-600 shadow-sm hover:bg-blue-50 transition"
              on:click={handleApply}
            >
              지원하기
            </button>
          {/if}
        {/if}
      </div>
    </div>
  </div>
  </div>

{/if}
