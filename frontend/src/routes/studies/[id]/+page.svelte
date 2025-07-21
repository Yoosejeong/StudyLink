<script lang="ts">
  import { onMount } from 'svelte';
  import { page } from '$app/stores';
  import { goto } from '$app/navigation';
  import { get } from 'svelte/store';
  import { isLoggedIn } from '$lib/stores/auth';

  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL;

  type StudyPost = {
    studyPostId: number;
    title: string;
    nickname: string; // ✅ 작성자 닉네임
    content: string;
    maxPeople: number;
    acceptedPeople: number;
    studyStatus: 'RECRUITING' | 'CLOSED';
    createdAt: string;
    updatedAt: string;
  };

  type CurrentUser = {
    nickname: string; // ✅ 로그인한 유저 닉네임
  };

  let studyPost: StudyPost | null = null;
  let currentUserNickname: string | null = null;
  let isLoading = true;
  let errorMessage = '';
  $: studyPostId = $page.params.id;

  onMount(async () => {
    if (!get(isLoggedIn)) {
      alert('로그인 후 이용해주세요.');
      goto('/auth');
      return;
    }

    await fetchUser();
    await fetchStudyPost();
  });

  async function fetchUser() {
    try {
      const token = localStorage.getItem('accessToken') || '';
      const accessToken = token.startsWith('Bearer ') ? token : `Bearer ${token}`;

      const res = await fetch(`${apiBaseUrl}/api/users`, {
        headers: {
          Authorization: accessToken,
        },
        credentials: 'include',
      });

      if (res.ok) {
        const data: CurrentUser = await res.json();
        currentUserNickname = data.result.nickname;
      } else {
        console.error('유저 정보 조회 실패');
      }
    } catch (err) {
      console.error('유저 정보 요청 실패', err);
    }
  }

  async function fetchStudyPost() {
    try {
      const token = localStorage.getItem('accessToken') || '';
      const accessToken = token.startsWith('Bearer ') ? token : `Bearer ${token}`;

      const res = await fetch(`${apiBaseUrl}/api/study-posts/${studyPostId}`, {
        method: 'GET',
        headers: {
          Authorization: accessToken,
        },
        credentials: 'include',
      });

      if (!res.ok) {
        const errorData = await res.json();
        errorMessage = errorData.message || `조회 실패 (code: ${res.status})`;
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

  function handleEdit() {
    goto(`/studies/${studyPostId}/edit`);
  }

  function handleDelete() {
    if (confirm('정말 삭제하시겠습니까?')) {
      alert('삭제 로직은 아직 구현되지 않았습니다.');
    }
  }
</script>

{#if isLoading}
  <p class="text-center mt-10 text-gray-500">불러오는 중...</p>
{:else if errorMessage}
  <p class="text-center mt-10 text-red-500">{errorMessage}</p>
{:else if studyPost}
  <div class="max-w-3xl mx-auto px-4 py-12">
    <div class="bg-white shadow-md rounded-lg p-8">
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
        {/if}
      </div>
    </div>
  </div>
{/if}
