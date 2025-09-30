<script lang="ts">
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { get } from 'svelte/store';
  import { isLoggedIn } from '$lib/stores/auth';
  import { http } from '$lib/api/http';

  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL;

  // 앞의 #들 제거 + trim
  const stripHash = (s: string) => s.replace(/^#+/, '').trim();
  // 캐논값(비교/저장/전송용): 해시 제거 + 소문자
  const canon = (s: string) => stripHash(s).toLowerCase();

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

  let tagInput: HTMLInputElement;        // 태그 입력칸 DOM
  let maxPeopleInput: HTMLInputElement;  // 최대 인원 입력칸 DOM
  let triedSubmit = false;               // 제출 시도 후 에러 표시 트리거
  $: invalidTags = tags.length === 0;    // 태그 최소 1개 필요

  // --- 태그 상태 & 로직 (자동완성 제거) ---
  let tags: string[] = [];
  let tagQ = '';
  const TAG_LIMIT = 5;
  const TAG_MIN = 1;
  const TAG_MAX = 20;

  let composing = false; // IME 조합중 여부 (Enter 오작동 방지)

  function canAddCandidate(raw: string) {
    const val = stripHash(raw); // 해시 제거 후 길이 검사
    const c = canon(raw);       // 비교/중복은 캐논값(소문자)
    if (!val) return false;
    if (val.length < TAG_MIN || val.length > TAG_MAX) return false;
    if (tags.length >= TAG_LIMIT) return false;
    if (tags.includes(c)) return false; // 이미 있으면 추가 불가
    return true;
  }

  function addTag(raw: string) {
    // 입력 정리
    const val = stripHash(raw);
    const c = canon(raw);

    if (val.length < TAG_MIN || val.length > TAG_MAX) return;
    if (tags.length >= TAG_LIMIT) return;
    if (tags.includes(c)) return;

    tags = [...tags, c];
    tagQ = '';
    tagInput?.setCustomValidity('');
  }

  function removeTag(i: number) {
    tags = tags.filter((_, idx) => idx !== i);
    if (triedSubmit && tags.length === 0) {
      tagInput?.setCustomValidity('태그를 최소 1개 이상 추가하세요.');
    } else {
      tagInput?.setCustomValidity('');
    }
  }

  function onTagKeydown(e: KeyboardEvent) {
    if ((e as any).isComposing || composing) return;
    if (e.key === 'Enter' || e.key === 'Tab' || e.key === ',') {
      e.preventDefault();
      addTag(tagQ);
    } else if (e.key === 'Backspace' && tagQ === '' && tags.length) {
      removeTag(tags.length - 1);
    }
  }

  // --- 제출 ---
  const handleSubmit = async (e: Event) => {
    e.preventDefault();
    triedSubmit = true;

    if (!category) {
      alert('카테고리를 선택해주세요.');
      return;
    }

    // ⬇️ 태그 최소 1개 필수
    if (tags.length === 0) {
      tagInput?.setCustomValidity('태그를 최소 1개 이상 추가하세요.');
      tagInput?.reportValidity();
      tagInput?.focus();
      return;
    } else {
      tagInput?.setCustomValidity('');
    }

    // ⬇️ 최대 인원 2~20 정수 필수
    const mp = Number(maxPeople);
    if (!Number.isFinite(mp) || !Number.isInteger(mp) || mp < 2 || mp > 20) {
      maxPeopleInput?.setCustomValidity('최대 인원수는 2~20 사이의 정수여야 합니다.');
      maxPeopleInput?.reportValidity();
      maxPeopleInput?.focus();
      return;
    } else {
      maxPeopleInput?.setCustomValidity('');
    }

    const payload = {
      title,
      content,
      category,
      maxPeople: mp,
      tags // 예: ['react','typescript'] (캐논값)
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
          class={`mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 shadow-sm
                  focus:border-blue-500 focus:ring-blue-500
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
          bind:this={maxPeopleInput}
          type="number"
          bind:value={maxPeople}
          required
          min="2"
          max="20"
          step="1"
          inputmode="numeric"
          placeholder="예: 6"
          class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-blue-500"
          on:input={() => maxPeopleInput?.setCustomValidity('')}
        />
        <p class="mt-1 text-sm text-gray-500">2명 이상 20명 이하의 정수로 설정해주세요</p>
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

      <!-- 태그 (칩스만; 자동완성 제거) -->
      <div>
        <label class="block text-sm font-medium text-gray-700">태그</label>

        <!-- chips -->
        <div class="mt-1 mb-2 flex flex-wrap gap-2">
          {#each tags as t, i}
            <span class="inline-flex items-center gap-1 rounded-full border border-gray-300 px-2 py-1 text-sm">
              #{t}
              <button
                type="button"
                class="opacity-60 hover:opacity-100"
                aria-label={`#${t} 제거`}
                on:click={() => removeTag(i)}
              >
                ×
              </button>
            </span>
          {/each}
        </div>

        <!-- input only -->
        <input
          bind:this={tagInput}
          class="w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-blue-500"
          bind:value={tagQ}
          placeholder={`태그 입력 후 Enter (최대 ${TAG_LIMIT}개)`}
          on:keydown={onTagKeydown}
          on:compositionstart={() => (composing = true)}
          on:compositionend={() => (composing = false)}
          on:input={() => {
            // 타이핑 시 에러 해제 시도
            tagInput?.setCustomValidity('');
          }}
          aria-invalid={triedSubmit && invalidTags}
          aria-describedby="tag-help"
        />
        <div class="mt-1 text-right text-xs text-gray-500">{tags.length}/{TAG_LIMIT}개</div>
      </div>

      <!-- 버튼 -->
      <div class="flex justify-end space-x-3">
        <button
          type="button"
          class="rounded bg-gray-200 px-4 py-2 text-gray-800 shadow-sm hover:bg-gray-300 active:border active:border-gray-300 active:bg-white active:text-gray-900"
          on:click={() => goto('/')}
        >
          취소
        </button>
        <button
          type="submit"
          class="rounded bg-blue-600 px-4 py-2 text-white shadow-sm hover:bg-blue-700 active:border active:border-gray-300 active:bg-white active:text-gray-900"
        >
          모집글 등록하기
        </button>
      </div>
    </form>
  </div>
</div>

