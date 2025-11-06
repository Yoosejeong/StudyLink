<!-- src/routes/mypage/applications/+page.svelte -->
<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { http } from '$lib/api/http';
	import { Calendar, Flag, Check, X, Archive } from 'lucide-svelte';

	// 서버 리턴 타입 (MyStudyApplicationResponse)
	type StudyStatus = 'RECRUITING' | 'CLOSED';
	type ApplicationStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'CANCELED';

	type MyStudyApplication = {
		studyApplicationId: number;
		studyPostId: number;
		studyTitle: string;
		studyStatus: StudyStatus;
		deleted: boolean;
		applicationStatus: ApplicationStatus;
		appliedAt: string; // ISO
	};

	let items: MyStudyApplication[] = [];
	let isLoading = true;
	let errorMessage = '';

	// 로컬 필터 (전체/대기/승인/거절)
	type FilterKey = 'ALL' | ApplicationStatus;
	let filter: FilterKey = 'ALL';

	onMount(loadData);

	async function loadData() {
		try {
			const res = await http.get('/api/users/applications');
			if (!res.ok) {
				errorMessage = (await safeMsg(res)) ?? `조회 실패 (code: ${res.status})`;
				return;
			}
			const data = await res.json();
			items = ((data?.result as MyStudyApplication[]) ?? []).sort(
				(a, b) => Number(a.deleted) - Number(b.deleted)
			);
		} catch {
			errorMessage = '네트워크 오류가 발생했습니다.';
		} finally {
			isLoading = false;
		}
	}

	function filtered() {
		if (filter === 'ALL') return items;
		return items.filter((i) => i.applicationStatus === filter);
	}

	function statusPill(s: ApplicationStatus) {
		return s === 'ACCEPTED'
			? 'bg-green-100 text-green-700'
			: s === 'REJECTED'
				? 'bg-red-100 text-red-700'
				: 'bg-blue-100 text-blue-700';
	}

	function rowDisabled(it: MyStudyApplication) {
		// 글이 삭제되었거나 스터디가 종료되었으면 살짝 흐리게
		return it.deleted ? 'opacity-60' : '';
	}

	function toDetail(it: MyStudyApplication) {
		if (it.deleted) return; // 삭제된 글은 이동 비활성
		goto(`/studies/${it.studyPostId}`);
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

<svelte:head><title>내 지원 목록</title></svelte:head>

{#if isLoading}
	<div class="flex h-64 items-center justify-center">
		<p class="text-gray-500">불러오는 중입니다…</p>
	</div>
{:else if errorMessage}
	<div class="mx-auto max-w-4xl px-4 py-10">
		<p class="rounded-md border border-red-100 bg-red-50 px-4 py-3 text-red-700">
			{errorMessage}
		</p>
	</div>
{:else}
	<div class="mx-auto max-w-5xl px-4 py-8">
		<div class="mb-6 rounded-2xl bg-white p-6 shadow-lg">
			<div class="flex items-center justify-between">
				<div>
					<h1 class="text-xl font-bold">내 지원 목록</h1>
					<p class="mt-1 text-sm text-gray-500">총 {items.length}건의 지원 내역</p>
				</div>

				<div class="inline-flex rounded-full border border-gray-200 bg-gray-50 p-1">
					<button
						class={'rounded-full px-3 py-1.5 text-sm ' +
							(filter === 'ALL' ? 'bg-white font-semibold shadow' : 'text-gray-600 hover:bg-white')}
						on:click={() => (filter = 'ALL')}
					>
						전체
					</button>
					<button
						class={'rounded-full px-3 py-1.5 text-sm ' +
							(filter === 'PENDING'
								? 'bg-white font-semibold shadow'
								: 'text-gray-600 hover:bg-white')}
						on:click={() => (filter = 'PENDING')}
					>
						대기
					</button>
					<button
						class={'rounded-full px-3 py-1.5 text-sm ' +
							(filter === 'ACCEPTED'
								? 'bg-white font-semibold shadow'
								: 'text-gray-600 hover:bg-white')}
						on:click={() => (filter = 'ACCEPTED')}
					>
						승인
					</button>
					<button
						class={'rounded-full px-3 py-1.5 text-sm ' +
							(filter === 'REJECTED'
								? 'bg-white font-semibold shadow'
								: 'text-gray-600 hover:bg-white')}
						on:click={() => (filter = 'REJECTED')}
					>
						거절
					</button>
					<button
						class={'rounded-full px-3 py-1.5 text-sm ' +
							(filter === 'CANCELED'
								? 'bg-white font-semibold shadow'
								: 'text-gray-600 hover:bg-white')}
						on:click={() => (filter = 'CANCELED')}
					>
						취소
					</button>
				</div>
			</div>
		</div>

		<div class="space-y-4">
			{#if filtered().length === 0}
				<div
					class="rounded-xl border border-dashed border-gray-300 bg-white p-10 text-center text-gray-500"
				>
					해당 조건의 지원 내역이 없습니다.
				</div>
			{:else}
				{#each filtered() as it}
					<div
						class={'group flex w-full cursor-pointer items-center justify-between rounded-xl bg-white p-5 shadow-sm transition hover:shadow-md ' +
							rowDisabled(it)}
						on:click={() => toDetail(it)}
						title={it.deleted ? '삭제된 글입니다' : '상세 보기'}
					>
						<div class="min-w-0 flex-1">
							<div class="mb-1 flex items-center gap-2">
								<h2 class="truncate text-lg font-semibold text-gray-900">{it.studyTitle}</h2>
								<span class={'rounded px-2 py-0.5 text-xs ' + statusPill(it.applicationStatus)}>
									{it.applicationStatus}
								</span>
								<span
									class="inline-block rounded px-2 py-0.5 text-xs"
									class:bg-yellow-100={it.studyStatus === 'RECRUITING'}
									class:text-yellow-800={it.studyStatus === 'RECRUITING'}
									class:bg-gray-300={it.studyStatus === 'CLOSED'}
									class:text-gray-800={it.studyStatus === 'CLOSED'}
								>
									{it.studyStatus === 'RECRUITING' ? 'RECRUITING' : 'CLOSED'}
								</span>
								{#if it.deleted}
									<span class="rounded bg-gray-200 px-2 py-0.5 text-xs text-gray-700">삭제됨</span>
								{/if}
							</div>

							<div class="flex flex-wrap items-center gap-x-4 gap-y-1 text-sm text-gray-500">
								<span class="inline-flex items-center gap-1">
									<Calendar class="h-4 w-4" />
									{new Date(it.appliedAt).toLocaleDateString('ko-KR')}
								</span>
							</div>
						</div>

						<div class="ml-4 shrink-0">
							{#if it.applicationStatus === 'ACCEPTED'}
								<Check class="h-5 w-5 text-green-600 opacity-80 group-hover:opacity-100" />
							{:else if it.applicationStatus === 'REJECTED'}
								<X class="h-5 w-5 text-red-600 opacity-80 group-hover:opacity-100" />
							{:else if it.applicationStatus === 'CANCELED'}
								<Archive class="h-5 w-5 text-gray-500 opacity-80 group-hover:opacity-100" />
							{:else}
								<Flag class="h-5 w-5 text-blue-600 opacity-80 group-hover:opacity-100" />
							{/if}
						</div>
					</div>
				{/each}
			{/if}
		</div>
	</div>
{/if}