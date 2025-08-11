<script lang="ts">
	import { onMount } from 'svelte';
	import { page } from '$app/stores';
	import { Check, X, Calendar, User } from 'lucide-svelte';
	import { http } from '$lib/api/http';

	$: studyPostId = $page.params.id;

	type Applicant = {
		studyApplicationId: number;
		userId: number;
		nickname: string;
		applicationStatus: 'PENDING' | 'ACCEPTED' | 'REJECTED';
		appliedAt: string;
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
				const err = await safeJson(res);
				errorMessage = err?.message || `조회 실패 (code: ${res.status})`;
				return;
			}
			const data = await res.json();
			applicants = data.result ?? [];
		} catch {
			errorMessage = '네트워크 오류 발생';
		} finally {
			isLoading = false;
		}
	}

	async function handleApprove(appId: number) {
		if (busyId) return;
		busyId = appId;
		try {
			const res = await http.patch(`/api/study-applications/${appId}/approve`);
			if (!res.ok) {
				const err = await safeJson(res);
				alert(`승인 실패: ${err?.message || res.status}`);
				return;
			}
			// 승인 완료 → 상태 갱신 → 버튼 자동 숨김
			applicants = applicants.map((a) =>
				a.studyApplicationId === appId ? { ...a, applicationStatus: 'ACCEPTED' } : a
			);
		} finally {
			busyId = null;
		}
	}

	async function handleReject(appId: number) {
		if (busyId) return;
		const ok = confirm('정말 거절하시겠습니까?');
		if (!ok) return;

		busyId = appId;
		try {
			const res = await http.patch(`/api/study-applications/${appId}/reject`);
			if (!res.ok) {
				const err = await safeJson(res);
				alert(`거절 실패: ${err?.message || res.status}`);
				return;
			}
			// 거절 완료 → 상태 갱신 → 버튼 자동 숨김
			applicants = applicants.map((a) =>
				a.studyApplicationId === appId ? { ...a, applicationStatus: 'REJECTED' } : a
			);
		} finally {
			busyId = null;
		}
	}

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

	const statusPill = (s: Applicant['applicationStatus']) =>
		s === 'APPROVED'
			? 'bg-green-100 text-green-800'
			: s === 'REJECTED'
				? 'bg-red-100 text-red-800'
				: 'bg-blue-100 text-blue-800';
</script>

{#if isLoading}
	<p class="mt-10 text-center text-gray-500">불러오는 중...</p>
{:else if errorMessage}
	<p class="mt-10 text-center text-red-500">{errorMessage}</p>
{:else}
	<div class="container mx-auto p-6">
		<div class="mx-auto max-w-6xl rounded-xl border bg-white shadow-lg">
			<div class="border-b p-6">
				<h2 class="flex items-center gap-2 text-xl font-bold">
					<User class="h-5 w-5" />
					스터디 지원자 목록
				</h2>
				<p class="mt-1 text-sm text-gray-500">총 {applicants.length}명이 지원했습니다</p>
			</div>

			<div class="space-y-4 p-6">
				{#if applicants.length === 0}
					<p class="text-center text-gray-500">아직 지원자가 없습니다.</p>
				{:else}
					{#each applicants as applicant}
						<div
							class="flex w-full items-center justify-between rounded-lg border p-4 transition-colors hover:bg-gray-50"
						>
							<!-- 왼쪽 -->
							<div class="flex flex-1 items-center gap-4">
								<div
									class="flex h-12 w-12 items-center justify-center rounded-full bg-gray-200 text-sm font-semibold text-gray-700"
								>
									{applicant.nickname.slice(0, 2)}
								</div>
								<div class="min-w-0 flex-1">
									<div class="mb-1 flex items-center gap-2">
										<h3 class="text-lg font-semibold">{applicant.nickname}</h3>
										<span
											class="rounded px-2 py-0.5 text-xs
                        {applicant.applicationStatus === 'ACCEPTED'
												? 'bg-green-100 text-green-800'
												: applicant.applicationStatus === 'REJECTED'
													? 'bg-red-100 text-red-800'
													: 'bg-blue-100 text-blue-800'}"
										>
											{applicant.applicationStatus}
										</span>
									</div>
								</div>
							</div>

							<!-- 오른쪽 -->
							<div class="flex items-center gap-3">
								<div class="hidden items-center gap-1 text-sm text-gray-500 sm:flex">
									<Calendar class="h-4 w-4" />
									{new Date(applicant.appliedAt).toLocaleDateString('ko-KR')}
								</div>

								<!-- 처리 여부에 따라 UI 분기 -->
								{#if applicant.applicationStatus === 'PENDING'}
									<div class="flex gap-2">
										<button
											class="flex h-8 w-8 items-center justify-center rounded border hover:border-green-200 hover:bg-green-50 disabled:opacity-50"
											on:click={() => handleApprove(applicant.studyApplicationId)}
											disabled={busyId === applicant.studyApplicationId}
											title="승인"
										>
											<Check class="h-4 w-4 text-green-600" />
										</button>
										<button
											class="flex h-8 w-8 items-center justify-center rounded border hover:border-red-200 hover:bg-red-50 disabled:opacity-50"
											on:click={() => handleReject(applicant.studyApplicationId)}
											disabled={busyId === applicant.studyApplicationId}
											title="거절"
										>
											<X class="h-4 w-4 text-red-600" />
										</button>
									</div>
								{:else}
									<div class="rounded border bg-gray-50 px-2 py-1 text-sm text-gray-500">
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
