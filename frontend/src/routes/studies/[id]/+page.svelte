<script lang="ts">
  import { onMount } from 'svelte';
  import { page } from '$app/stores';
  import { goto } from '$app/navigation';
  import { get } from 'svelte/store';
  import { isLoggedIn } from '$lib/stores/auth';
  import { http } from '$lib/api/http';
  import { registerOnReissueFail } from '$lib/api/fetchWithAuth';

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
    <div class="bg-white shadow-md rounded-lg p-8 relative">
      <!-- ✅ 오른쪽 상단 버튼 (자기 글만) -->
      {#if currentUserId === studyPost.userId}
  <button
    class="absolute top-4 right-4 px-3 py-1.5 text-sm border border-gray-300 rounded-lg bg-white hover:bg-gray-100 shadow-sm flex items-center gap-1 transition"
    on:click={() => goto(`/studies/${studyPostId}/applications`)}
  >
    👥 지원자 목록
  </button>
{/if}

      <h1 class="text-3xl font-bold text-gray-900 mb-2">{studyPost.title}</h1>
      <p class="text-gray-600 mb-6">
        작성자: <span class="font-medium">{studyPost.nickname}</span> |
        등록일: <span>{new Date(studyPost.createdAt).toLocaleString()}</span>
      </p>

      <div class="mb-6">
        <label class="block text-sm font-medium text-gray-700 mb-1">스터디 소개</label>
        <div class="border border-gray-300 rounded-md p-4 whitespace-pre-line text-gray-800 bg-gray-50">
          {studyPost.content}
        </div>
      </div>

      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700">최대 인원</label>
        <p class="mt-1 text-gray-800">{studyPost.maxPeople}명</p>
      </div>

      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700">현재 승인 인원</label>
        <p class="mt-1 text-gray-800">{studyPost.acceptedPeople}명</p>
      </div>

      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700">모집 상태</label>
        <p class="mt-1 text-gray-800">
          {studyPost.studyStatus === 'RECRUITING' ? '모집 중' : '모집 종료'}
        </p>
      </div>

      <div class="flex justify-between items-center mt-8">
        <button
          class="bg-gray-200 text-gray-800 px-4 py-2 rounded hover:bg-gray-300"
          on:click={() => goto('/')}
        >
          목록으로 돌아가기
        </button>

        {#if currentUserNickname === studyPost.nickname}
          <div class="flex space-x-3">
            <button
              class="bg-gray-200 text-gray-800 px-4 py-2 rounded hover:bg-gray-300"
              on:click={handleEdit}
            >
              수정하기
            </button>
            <button
              class="bg-gray-200 text-gray-800 px-4 py-2 rounded hover:bg-gray-300"
              on:click={handleDelete}
            >
              삭제하기
            </button>
          </div>
        {:else}
          {#if hasApplied}
            <div class="flex items-center space-x-3 bg-green-50 border border-green-300 px-4 py-2 rounded-lg">
              <span class="text-green-600 font-semibold">이미 지원한 스터디입니다.</span>
              {#if applicationStatus}
                <span class="text-sm text-green-600 bg-white px-2 py-0.5 rounded-full border border-green-300">
                  상태: {applicationStatus}
                </span>
              {/if}
            </div>
          {:else if studyPost.studyStatus === 'RECRUITING'}
            <button
              class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
              on:click={handleApply}
            >
              지원하기
            </button>
          {/if}
        {/if}
      </div>
    </div>
  </div>
{/if}