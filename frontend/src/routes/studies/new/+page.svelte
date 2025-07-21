<script lang="ts">
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { isLoggedIn } from '$lib/stores/auth';

  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL;

  onMount(() => {
    if (!$isLoggedIn) {
      alert("로그인 후 작성 가능합니다.");
      goto('/auth');
    }
  });

  let title = '';
  let content = '';
  let maxPeople = '';

  const handleSubmit = async (e: Event) => {
    e.preventDefault();

    const payload = {
      title,
      content,
      maxPeople: Number(maxPeople),
    };

    const rawToken = localStorage.getItem('accessToken') || '';
    const accessToken = rawToken.startsWith('Bearer ') ? rawToken.slice(7) : rawToken;

    try {
      const res = await fetch(`${apiBaseUrl}/api/study-posts`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${accessToken}`,
        },
        credentials: 'include',
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        const errorData = await res.json();
        alert(`오류 발생: ${errorData.message || res.status}`);
        return;
      }

      alert('스터디 모집글이 등록되었습니다!');
      goto('/');
    } catch (err) {
      console.error(err);
      alert('네트워크 오류 발생');
    }
  };
</script>

<div class="max-w-3xl mx-auto px-4 py-12">
  <div class="bg-white shadow-md rounded-lg p-8">
    <h1 class="text-3xl font-bold text-gray-900 mb-2">스터디 모집하기</h1>
    <p class="text-gray-600 mb-6">함께 공부할 팀원들을 모집해보세요!</p>

    <form on:submit={handleSubmit} class="space-y-6">
      <!-- 제목 -->
      <div>
        <label for="title" class="block text-sm font-medium text-gray-700">스터디 제목 *</label>
        <input
          id="title"
          type="text"
          bind:value={title}
          required
          placeholder="예: React 스터디원 모집합니다"
          class="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 shadow-sm focus:ring-blue-500 focus:border-blue-500"
        />
      </div>

      <!-- 최대 인원 -->
      <div>
        <label for="maxPeople" class="block text-sm font-medium text-gray-700">최대 인원수 *</label>
        <input
          id="maxPeople"
          type="number"
          bind:value={maxPeople}
          required
          min="2"
          max="20"
          placeholder="예: 6"
          class="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 shadow-sm focus:ring-blue-500 focus:border-blue-500"
        />
        <p class="text-sm text-gray-500 mt-1">2명 이상 20명 이하로 설정해주세요</p>
      </div>

      <!-- 소개 -->
      <div>
        <label for="content" class="block text-sm font-medium text-gray-700">스터디 소개 *</label>
        <textarea
          id="content"
          bind:value={content}
          required
          placeholder="스터디에 대한 자세한 설명을 작성해주세요. 예: 목표, 진행 방식, 일정 등"
          class="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 h-40 shadow-sm resize-none focus:ring-blue-500 focus:border-blue-500"
        ></textarea>
      </div>

      <!-- 버튼 -->
      <div class="flex justify-end space-x-3">
        <button type="button" class="bg-gray-200 text-gray-800 px-4 py-2 rounded hover:bg-gray-300" on:click={() => goto('/')}>
          취소
        </button>
        <button type="submit" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">
          모집글 등록하기
        </button>
      </div>
    </form>
  </div>
</div>
