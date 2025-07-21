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
    nickname: string;
    content: string;
    maxPeople: number;
    acceptedPeople: number;
    studyStatus: 'RECRUITING' | 'CLOSED';
    createdAt: string;
    updatedAt: string;
  };

  let studyPost: StudyPost | null = null;
  let isLoading = true;
  let errorMessage = '';
  $: studyPostId = $page.params.id;

  onMount(() => {
    if (!get(isLoggedIn)) {
      alert('로그인 후 이용해주세요.');
      goto('/auth');
      return;
    }

    fetchStudyPost();
  });

  async function fetchStudyPost() {
    try {
      const rawToken = localStorage.getItem('accessToken') || '';
      const accessToken = rawToken.startsWith('Bearer ')
        ? rawToken
        : `Bearer ${rawToken}`;

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

      <!-- 소개 -->
      <div class="mb-6">
        <label class="block text-sm font-medium text-gray-700 mb-1">스터디 소개</label>
        <div class="border border-gray-300 rounded-md p-4 whitespace-pre-line text-gray-800 bg-gray-50">
          {studyPost.content}
        </div>
      </div>

      <!-- 최대 인원 -->
      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700">최대 인원</label>
        <p class="mt-1 text-gray-800">{studyPost.maxPeople}명</p>
      </div>

      <!-- 현재 인원 -->
      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700">현재 승인 인원</label>
        <p class="mt-1 text-gray-800">{studyPost.acceptedPeople}명</p>
      </div>

      <!-- 모집 상태 -->
      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700">모집 상태</label>
        <p class="mt-1 text-gray-800">
          {studyPost.studyStatus === 'RECRUITING' ? '모집 중' : '모집 종료'}
        </p>
      </div>

      <!-- 버튼 -->
      <div class="flex justify-end space-x-3 mt-8">
        <button
          class="bg-gray-200 text-gray-800 px-4 py-2 rounded hover:bg-gray-300"
          on:click={() => goto('/')}
        >
          목록으로 돌아가기
        </button>
      </div>
    </div>
  </div>
{/if}
