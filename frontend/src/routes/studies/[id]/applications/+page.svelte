<script lang="ts">
  import { onMount } from 'svelte';
  import { page } from '$app/stores';
  import { Check, X, Calendar, User } from 'lucide-svelte';
  import { http } from '$lib/api/http'; 

  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL;
  $: studyPostId = $page.params.id;

  type Applicant = {
    studyApplicationId: number;
    userId: number;
    nickname: string;
    applicationStatus: string;
    appliedAt: string;
  };

  let applicants: Applicant[] = [];
  let isLoading = true;
  let errorMessage = '';

  // ✅ 지원자 목록 불러오기 (토큰/재발급 자동)
  onMount(async () => {
    try {
      const res = await http.get(`/api/study-posts/${studyPostId}/applications`);
      if (!res.ok) {
        const err = await safeJson(res);
        errorMessage = err?.message || `조회 실패 (code: ${res.status})`;
        return;
      }
      const data = await res.json();
      applicants = data.result ?? [];
    } catch (err) {
      console.error('지원자 목록 조회 실패:', err);
      errorMessage = '네트워크 오류 발생';
    } finally {
      isLoading = false;
    }
  });

  // 본문이 없을 수도 있으니 안전 파서
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
{:else}
  <div class="container mx-auto p-6">
    <!-- 카드 전체 -->
    <div class="max-w-6xl mx-auto bg-white rounded-xl shadow-lg border">
      <!-- 헤더 -->
      <div class="border-b p-6">
        <h2 class="flex items-center gap-2 text-xl font-bold">
          <User class="h-5 w-5" />
          스터디 지원자 목록
        </h2>
        <p class="text-gray-500 text-sm mt-1">총 {applicants.length}명이 지원했습니다</p>
      </div>

      <!-- 리스트 -->
      <div class="p-6 space-y-4">
        {#if applicants.length === 0}
          <p class="text-center text-gray-500">아직 지원자가 없습니다.</p>
        {:else}
          {#each applicants as applicant}
            <div class="w-full flex items-center justify-between p-4 border rounded-lg hover:bg-gray-50 transition-colors">
              <!-- 왼쪽 프로필 -->
              <div class="flex items-center gap-4 flex-1">
                <div class="h-12 w-12 rounded-full bg-gray-200 flex items-center justify-center text-gray-700 text-sm font-semibold">
                  {applicant.nickname.slice(0, 2)}
                </div>

                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2 mb-1">
                    <h3 class="font-semibold text-lg">{applicant.nickname}</h3>
                    <span class="px-2 py-0.5 text-xs rounded bg-blue-100 text-blue-800">
                      {applicant.applicationStatus}
                    </span>
                  </div>
                </div>
              </div>

              <!-- 오른쪽 지원일 & 버튼 -->
              <div class="flex items-center gap-3">
                <div class="hidden sm:flex items-center gap-1 text-sm text-gray-500">
                  <Calendar class="h-4 w-4" />
                  {new Date(applicant.appliedAt).toLocaleDateString("ko-KR")}
                </div>

                <div class="flex gap-2">
                  <button class="h-8 w-8 border rounded flex items-center justify-center hover:bg-green-50 hover:border-green-200">
                    <Check class="h-4 w-4 text-green-600" />
                  </button>
                  <button class="h-8 w-8 border rounded flex items-center justify-center hover:bg-red-50 hover:border-red-200">
                    <X class="h-4 w-4 text-red-600" />
                  </button>
                </div>
              </div>
            </div>
          {/each}
        {/if}
      </div>
    </div>
  </div>
{/if}
