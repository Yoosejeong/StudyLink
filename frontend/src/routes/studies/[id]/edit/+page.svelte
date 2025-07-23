<script lang="ts">
  import { onMount } from 'svelte';
  import { page } from '$app/stores';
  import { goto } from '$app/navigation';

  let title = '';
  let content = '';
  let studyPostId: number;

  const baseUrl = import.meta.env.VITE_API_BASE_URL;

  // 기존 데이터 불러오기
  onMount(async () => {
    const id = Number($page.url.pathname.split('/')[2]);
    studyPostId = id;

    const token = localStorage.getItem('accessToken');
    if (!token) {
      alert('로그인이 필요합니다.');
      goto('/auth');
      return;
    }

    const accessToken = token.startsWith('Bearer ') ? token : `Bearer ${token}`;

    try {
      const res = await fetch(`${baseUrl}/api/study-posts/${studyPostId}`, {
        method: 'GET',
        headers: {
          'Authorization': accessToken,
          'Content-Type': 'application/json'
        }
      });

      if (!res.ok) {
        alert('데이터 로딩 실패');
        return;
      }

      const data = await res.json();
      title = data.result.title;
      content = data.result.content;
    } catch (err) {
      console.error('❌ 요청 오류:', err);
      alert('서버 요청 중 오류 발생');
    }
  });

  // 수정 요청
  async function updateStudy(e: Event) {
    e.preventDefault();

    const token = localStorage.getItem('accessToken') || '';
    const accessToken = token.startsWith('Bearer ') ? token : `Bearer ${token}`;

    const res = await fetch(`${baseUrl}/api/study-posts/${studyPostId}`, {
      method: 'PATCH',
      headers: {
        'Authorization': accessToken,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ title, content })
    });

    if (!res.ok) {
      alert('수정 실패');
      return;
    }

    alert('스터디 글이 수정되었습니다!');
    goto(`/studies/${studyPostId}`);
  }
</script>

<div class="max-w-3xl mx-auto px-4 py-12">
  <div class="bg-white shadow-md rounded-lg p-8">
    <h1 class="text-3xl font-bold text-gray-900 mb-2">스터디 글 수정</h1>
    <p class="text-gray-600 mb-6">스터디 글의 내용을 수정하세요</p>

    <form on:submit={updateStudy} class="space-y-6">
      <!-- 제목 -->
      <div>
        <label for="title" class="block text-sm font-medium text-gray-700">스터디 제목 *</label>
        <input
          id="title"
          type="text"
          bind:value={title}
          required
          class="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 shadow-sm focus:ring-blue-500 focus:border-blue-500"
        />
      </div>

      <!-- 소개 -->
      <div>
        <label for="content" class="block text-sm font-medium text-gray-700">스터디 소개 *</label>
        <textarea
          id="content"
          bind:value={content}
          required
          rows="8"
          class="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 shadow-sm resize-none focus:ring-blue-500 focus:border-blue-500"
        ></textarea>
      </div>

      <!-- 버튼 -->
      <div class="flex justify-end space-x-3">
        <button type="button" class="bg-gray-200 text-gray-800 px-4 py-2 rounded hover:bg-gray-300" on:click={() => goto('/')}>
          취소
        </button>
        <button type="submit" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">
          수정 완료
        </button>
      </div>
    </form>
  </div>
</div>
