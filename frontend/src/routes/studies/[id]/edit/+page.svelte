<script lang="ts">
  import { onMount } from 'svelte';
  import { page } from '$app/stores';
  import { goto } from '$app/navigation';
  import { get } from 'svelte/store';
  import { http } from '$lib/api/http'; 

  let title = '';
  let content = '';
  let studyPostId: number;

  // 204 등 본문 없을 때 대비
  async function safeJson(res: Response) {
    const text = await res.text();
    if (!text) return null;
    try { return JSON.parse(text); } catch { return null; }
  }

  // 기존 데이터 불러오기
  onMount(async () => {
    const idStr = get(page).params?.id ?? get(page).url.pathname.split('/')[2];
    studyPostId = Number(idStr);

    try {
      const res = await http.get(`/api/study-posts/${studyPostId}`);
      if (!res.ok) {
        const err = await safeJson(res);
        alert(`데이터 로딩 실패: ${err?.message || res.status}`);
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
    try {
      const res = await http.patch(`/api/study-posts/${studyPostId}`, { title, content });
      if (!res.ok) {
        const err = await safeJson(res);
        alert(`수정 실패: ${err?.message || res.status}`);
        return;
      }
      alert('스터디 글이 수정되었습니다!');
      goto(`/studies/${studyPostId}`);
    } catch (err) {
      console.error('❌ 수정 요청 오류:', err);
      alert('네트워크 오류로 수정에 실패했습니다.');
    }
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
