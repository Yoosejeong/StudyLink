<script lang="ts">
  import { onMount } from 'svelte';
  import { page } from '$app/stores';
  import { Check, X, Calendar, User } from 'lucide-svelte';
  import { http } from '$lib/api/http';

  $: studyPostId = $page.params.id;


  const DEFAULT_AVATAR = '/avatars/default.jpg';

  type Applicant = {
    studyApplicationId: number;
    userId: number;
    nickname: string;
    applicationStatus: 'PENDING' | 'ACCEPTED' | 'REJECTED';
    appliedAt: string;        
    profileUrl: string | null; 
  };

  let applicants: Applicant[] = [];
  let isLoading = true;
  let errorMessage = '';
  let busyId: number | null = null;

  onMount(loadApplicants);

  async function loadApplicants() {
    try {
      const res = await http.get(`/api/study-posts/${studyPostId}/applications`);
      if (!res.ok) {
        errorMessage = (await safeMsg(res)) ?? `조회 실패 (code: ${res.status})`;
        return;
      }
      const data = await res.json();
      applicants = (data?.result as Applicant[]) ?? [];
    } catch {
      errorMessage = '네트워크 오류 발생';
    } finally {
      isLoading = false;
    }
  }

  function avatarSrc(a: Applicant) {
    return a.profileUrl || undefined;
  }

  function onImgError(e: Event) {
    const img = e.target as HTMLImageElement;
    img.src = DEFAULT_AVATAR;
  }

  function initials(name: string) {
    return (name ?? '').slice(0, 2) || '?';
  }

  const pill = (s: Applicant['applicationStatus']) =>
    s === 'ACCEPTED'
      ? 'bg-green-100 text-green-800'
      : s === 'REJECTED'
      ? 'bg-red-100 text-red-800'
      : 'bg-blue-100 text-blue-800';

  async function handleApprove(id: number) {
    if (busyId) return;
    busyId = id;
    try {
      const res = await http.patch(`/api/study-applications/${id}/approve`);
      if (!res.ok) {
        alert((await safeMsg(res)) ?? '승인 실패');
        return;
      }
      applicants = applicants.map((a) =>
        a.studyApplicationId === id ? { ...a, applicationStatus: 'ACCEPTED' } : a
      );
    } finally {
      busyId = null;
    }
  }

  async function handleReject(id: number) {
    if (busyId) return;
    if (!confirm('정말 거절하시겠습니까?')) return;
    busyId = id;
    try {
      const res = await http.patch(`/api/study-applications/${id}/reject`);
      if (!res.ok) {
        alert((await safeMsg(res)) ?? '거절 실패');
        return;
      }
      applicants = applicants.map((a) =>
        a.studyApplicationId === id ? { ...a, applicationStatus: 'REJECTED' } : a
      );
    } finally {
      busyId = null;
    }
  }

  async function safeMsg(res: Response) {
    const text = await res.text();
    if (!text) return null;
    try {
      const j = JSON.parse(text);
      return j?.message ?? null;
    } catch {
      return null;
    }
  }
</script>

{#if isLoading}
  <p class="mt-10 text-center text-gray-500">불러오는 중...</p>
{:else if errorMessage}
  <p class="mt-10 text-center text-red-500">{errorMessage}</p>
{:else}
  <div class="container mx-auto p-6">
    <div class="mx-auto max-w-6xl rounded-2xl bg-white shadow-xl">
      <div class="border-b border-gray-100 p-6">
        <h2 class="flex items-center gap-2 text-xl font-bold">
          <User class="h-5 w-5" />
          스터디 지원자 목록
        </h2>
        <p class="mt-1 text-sm text-gray-500">
          총 {applicants.length}명이 지원했습니다
        </p>
      </div>

      <div class="space-y-4 p-6">
        {#if applicants.length === 0}
          <p class="text-center text-gray-500">아직 지원자가 없습니다.</p>
        {:else}
          {#each applicants as applicant}
            <div class="flex w-full items-center justify-between rounded-xl bg-white p-4 shadow-sm transition hover:shadow-md">
              <!-- 아바타 + 닉네임 -->
              <div class="flex flex-1 items-center gap-4">
                {#if avatarSrc(applicant)}
                  <img
                    src={avatarSrc(applicant)}
                    alt="profile"
                    class="h-12 w-12 shrink-0 rounded-full object-cover"
                    on:error={onImgError}          
                  />
                {:else}
                  <div class="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-gray-200 text-sm font-semibold text-gray-700">
                    {initials(applicant.nickname)}
                  </div>
                {/if}

                <div class="min-w-0 flex-1">
                  <div class="mb-1 flex items-center gap-2">
                    <h3 class="truncate text-lg font-semibold">{applicant.nickname}</h3>
                    <span class={`rounded px-2 py-0.5 text-xs ${pill(applicant.applicationStatus)}`}>
                      {applicant.applicationStatus}
                    </span>
                  </div>
                  <p class="text-sm text-gray-500">
                    {new Date(applicant.appliedAt).toLocaleDateString('ko-KR')}
                  </p>
                </div>
              </div>

              <!-- 승인/거절 버튼 -->
              <div class="flex items-center gap-2">
                {#if applicant.applicationStatus === 'PENDING'}
                  <button
                    class="flex h-8 w-8 items-center justify-center rounded-full bg-green-50 hover:bg-green-100"
                    on:click={() => handleApprove(applicant.studyApplicationId)}
                  >
                    <Check class="h-4 w-4 text-green-600" />
                  </button>
                  <button
                    class="flex h-8 w-8 items-center justify-center rounded-full bg-red-50 hover:bg-red-100"
                    on:click={() => handleReject(applicant.studyApplicationId)}
                  >
                    <X class="h-4 w-4 text-red-600" />
                  </button>
                {:else}
                  <div class="rounded bg-gray-100 px-2 py-1 text-sm text-gray-500">
                    이미 처리된 지원입니다.
                  </div>
                {/if}
              </div>
            </div>
          {/each}
        {/if}
      </div>
    </div>
  </div>
{/if}