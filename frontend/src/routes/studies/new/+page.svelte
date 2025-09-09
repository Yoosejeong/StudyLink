<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { get } from 'svelte/store';
	import { isLoggedIn } from '$lib/stores/auth';
	import { http } from '$lib/api/http';

	const apiBaseUrl = import.meta.env.VITE_API_BASE_URL;

	type CategoryCode =
		| 'BACKEND'
		| 'FRONTEND'
		| 'MOBILE'
		| 'DATA_AI'
		| 'CLOUD_DEVOPS'
		| 'ALGORITHM_CS'
		| 'GAME'
		| 'CERT';

	const CATEGORIES: { code: CategoryCode; label: string }[] = [
		{ code: 'BACKEND', label: '백엔드' },
		{ code: 'FRONTEND', label: '프론트엔드' },
		{ code: 'MOBILE', label: '모바일' },
		{ code: 'DATA_AI', label: '데이터·AI' },
		{ code: 'CLOUD_DEVOPS', label: '클라우드·DevOps' },
		{ code: 'ALGORITHM_CS', label: '알고리즘·CS' },
		{ code: 'GAME', label: '게임·그래픽' },
		{ code: 'CERT', label: '자격증' }
	];
	let category: CategoryCode | '' = '';

	onMount(() => {
		if (!get(isLoggedIn)) {
			alert('로그인 후 작성 가능합니다.');
			goto('/auth');
		}
	});

	let title = '';
	let content = '';
	let maxPeople = '';

	const handleSubmit = async (e: Event) => {
		e.preventDefault();
		if (!category) {
			alert('카테고리를 선택해주세요.');
			return;
		}

		const payload = {
			title,
			content,
			category,
			maxPeople: Number(maxPeople)
		};

		try {
			const res = await http.post('/api/study-posts', payload);

			if (!res.ok) {
				const errorData = await safeJson(res);
				alert(`오류 발생: ${errorData?.message || res.status}`);
				return;
			}

			alert('스터디 모집글이 등록되었습니다!');
			goto('/');
		} catch (err) {
			console.error(err);
			alert('네트워크 오류 발생');
		}
	};

	// 본문이 없을 수도 있으니 안전 파서
	async function safeJson(res: Response) {
		const text = await res.text();
		if (!text) return null;
		try {
			return JSON.parse(text);
		} catch {
			return null;
		}
	}
</script>
<div class="mx-auto max-w-3xl px-4 py-12">
  <div class="rounded-lg bg-white p-8 shadow-md">
    <h1 class="mb-2 text-3xl font-bold text-gray-900">스터디 모집하기</h1>
    <p class="mb-6 text-gray-600">함께 공부할 팀원들을 모집해보세요!</p>

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
          class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-blue-500"
        />
      </div>

      <!-- 카테고리 -->
      <div>
        <label for="category" class="block text-sm font-medium text-gray-700">카테고리 *</label>
        <select
  id="category"
  bind:value={category}          
  required
  class={`mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm
          focus:border-blue-500 focus:ring-blue-500 bg-white
          ${category ? 'text-gray-900' : 'text-gray-500'}`} 
  aria-invalid={!category}
>
  <option value="" disabled hidden>카테고리를 선택하세요</option>
  {#each CATEGORIES as c}
    <option value={c.code}>{c.label}</option>
  {/each}
</select>
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
          class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-blue-500"
        />
        <p class="mt-1 text-sm text-gray-500">2명 이상 20명 이하로 설정해주세요</p>
      </div>

      <!-- 소개 -->
      <div>
        <label for="content" class="block text-sm font-medium text-gray-700">스터디 소개 *</label>
        <textarea
          id="content"
          bind:value={content}
          required
          placeholder="스터디에 대한 자세한 설명을 작성해주세요. 예: 목표, 진행 방식, 일정 등"
          class="mt-1 block h-40 w-full resize-none rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-blue-500"
        ></textarea>
      </div>

      <!-- 버튼 -->
      <div class="flex justify-end space-x-3">
        <button
          type="button"
          class="rounded bg-gray-200 px-4 py-2 text-gray-800 shadow-sm hover:bg-gray-300 active:bg-white active:text-gray-900 active:border active:border-gray-300"
          on:click={() => goto('/')}
        >
          취소
        </button>
        <button
          type="submit"
          class="rounded bg-blue-600 px-4 py-2 text-white shadow-sm hover:bg-blue-700 active:bg-white active:text-gray-900 active:border active:border-gray-300"
        >
          모집글 등록하기
        </button>
      </div>
    </form>
  </div>
</div>
