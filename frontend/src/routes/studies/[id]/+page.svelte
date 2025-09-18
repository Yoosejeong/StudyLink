<script lang="ts">
	import { onMount } from 'svelte';
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import { get } from 'svelte/store';
	import { isLoggedIn } from '$lib/stores/auth';
	import { http } from '$lib/api/http';
	import { registerOnReissueFail } from '$lib/api/fetchWithAuth';
	import { ArrowLeft, Users, UserCheck, Flag } from 'lucide-svelte';

	type CategoryCode =
		| 'BACKEND'
		| 'FRONTEND'
		| 'MOBILE'
		| 'DATA_AI'
		| 'CLOUD_DEVOPS'
		| 'ALGORITHM_CS'
		| 'GAME'
		| 'CERT';

	const CAT_LABEL: Record<CategoryCode, string> = {
		BACKEND: '백엔드',
		FRONTEND: '프론트엔드',
		MOBILE: '모바일',
		DATA_AI: '데이터·AI',
		CLOUD_DEVOPS: '클라우드·DevOps',
		ALGORITHM_CS: '알고리즘·CS',
		GAME: '게임·그래픽',
		CERT: '자격증'
	};

	type StudyPost = {
		studyPostId: number;
		title: string;
		userId: number;
		nickname: string;
		content: string;
		maxPeople: number;
		acceptedPeople: number;
		studyStatus: 'RECRUITING' | 'CLOSED';
		category: { code: CategoryCode; label: string } | CategoryCode | string;
		tags?: string[];
		createdAt: string;
		updatedAt: string;
	};

	type CurrentUser = {
		userId: number;
		nickname: string;
	};

	let studyPost: StudyPost | null = null;
	let currentUserId: number | null = null;
	let currentUserNickname: string | null = null;
	let isLoading = true;
	let errorMessage = '';
	$: studyPostId = $page.params.id;
	let hasApplied = false;
	let applicationStatus: string | null = null;

	registerOnReissueFail(() => {
		localStorage.removeItem('accessToken');
		isLoggedIn.set(false);
		alert('세션이 만료되었습니다. 다시 로그인해주세요.');
		goto('/auth');
	});

	onMount(async () => {
		if (!get(isLoggedIn)) {
			alert('로그인 후 이용해주세요.');
			goto('/auth');
			return;
		}

		await fetchUser();
		await fetchStudyPost();
		await checkApplicationStatus();
	});

	function handleEdit() {
		goto(`/studies/${studyPostId}/edit`);
	}

	async function fetchUser() {
		try {
			const res = await http.get('/api/users');
			if (res.ok) {
				const data: { result: CurrentUser } = await res.json();
				currentUserId = data.result.userId;
				currentUserNickname = data.result.nickname;
			} else {
				console.error('유저 정보 조회 실패', res.status);
			}
		} catch (err) {
			console.error('유저 정보 요청 실패', err);
		}
	}

	async function handleApply() {
		try {
			const res = await http.post(`/api/study-posts/${studyPostId}/applications`);
			if (!res.ok) {
				const errorData = await safeJson(res);
				alert(`지원 실패: ${errorData?.message || res.status}`);
				return;
			}
			alert('지원이 완료되었습니다.');
			await checkApplicationStatus();
		} catch (err) {
			console.error('지원 요청 오류', err);
			alert('네트워크 오류로 지원에 실패했습니다.');
		}
	}

	async function checkApplicationStatus() {
		try {
			const res = await http.get(`/api/study-applications/check?studyPostId=${studyPostId}`);
			if (res.ok) {
				const data = await res.json();
				hasApplied = data.result.hasApplied;
				applicationStatus = data.result.applicationStatus;
			} else {
				console.error('지원 여부 확인 실패', res.status);
			}
		} catch (err) {
			console.error('지원 여부 요청 오류', err);
		}
	}

	async function fetchStudyPost() {
		try {
			const res = await http.get(`/api/study-posts/${studyPostId}`);
			if (!res.ok) {
				const errorData = await safeJson(res);
				errorMessage = errorData?.message || `조회 실패 (code: ${res.status})`;
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

	async function handleDelete() {
		const confirmed = confirm('정말 삭제하시겠습니까?');
		if (!confirmed) return;

		try {
			const res = await http.delete(`/api/study-posts/${studyPostId}`);
			if (!res.ok) {
				const errorData = await safeJson(res);
				alert(`삭제 실패: ${errorData?.message || res.status}`);
				return;
			}
			alert('삭제가 완료되었습니다.');
			goto('/');
		} catch (err) {
			console.error('삭제 요청 오류:', err);
			alert('네트워크 오류로 삭제에 실패했습니다.');
		}
	}

	async function handleCloseRecruitment() {
		const ok = confirm('정말 모집을 종료하시겠습니까?');
		if (!ok) return;

		try {
			const res = await http.patch(`/api/study-posts/${studyPostId}/close`);
			if (!res.ok) {
				const err = await safeJson(res);
				alert(`모집 종료 실패: ${err?.message || res.status}`);
				return;
			}
			alert('모집이 종료되었습니다.');
			// UI 갱신
			if (studyPost) studyPost.studyStatus = 'CLOSED';
		} catch (e) {
			console.error('모집 종료 요청 오류:', e);
			alert('네트워크 오류로 모집 종료에 실패했습니다.');
		}
	}

	// 204 등 본문 없는 응답 대비
	async function safeJson(res: Response) {
		const text = await res.text();
		if (!text) return null;
		try {
			return JSON.parse(text);
		} catch {
			return null;
		}
	}

	$: categoryLabel = studyPost
		? typeof studyPost.category === 'object'
			? studyPost.category.label
			: (CAT_LABEL[studyPost.category as CategoryCode] ?? (studyPost.category as string))
		: '';
</script>

{#if isLoading}
	<p class="mt-10 text-center text-gray-500">불러오는 중...</p>
{:else if errorMessage}
	<p class="mt-10 text-center text-red-500">{errorMessage}</p>
{:else if studyPost}
	<div class="mx-auto max-w-3xl px-4 py-12">
		<div class="rounded-lg bg-white p-8 shadow-md">
			<!-- 인라인 뒤로가기 -->
			<div class="mb-3">
				<button
					type="button"
					class="inline-flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700"
					on:click={() => goto('/')}
				>
					<ArrowLeft class="h-5 w-5" />
					<span>목록</span>
				</button>
			</div>

			<!-- 헤더: 카테고리 라벨 + 제목  -->
			<div class="mb-2 flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
				<div class="flex-1">
					<div class="mb-1">
						<span class="ml-2 inline-block rounded bg-indigo-100 px-2 py-1 text-xs text-indigo-800">
							{categoryLabel}
						</span>
					</div>

					<h1 class="text-3xl leading-snug font-extrabold text-gray-900 sm:max-w-xl md:text-4xl">
						{studyPost.title}
					</h1>
				</div>
			</div>

			<!-- 작성자 · 날짜 -->
			<div class="text-sm text-gray-600 md:text-base">
				작성자 <span class="font-medium text-gray-800">{studyPost.nickname}</span>
				<span class="mx-2 text-gray-300">·</span>
				등록일 <span class="text-gray-700">{new Date(studyPost.createdAt).toLocaleString()}</span>
			</div>

			<!-- 구분선 -->
			<div class="my-6 h-px bg-gray-200"></div>

			<!-- 스터디 소개 -->
			<section class="mb-8">
				<h2 class="mb-2 text-sm font-semibold text-gray-700">스터디 소개</h2>
				<div
					class="rounded-xl border border-gray-200 bg-white/70 p-5 leading-relaxed whitespace-pre-line text-gray-900"
				>
					{studyPost.content}
				</div>
			</section>

			{#if studyPost.tags && studyPost.tags.length}
				<!-- 태그 영역 -->
				<section class="mb-6">
					<h2 class="mb-2 text-sm font-semibold text-gray-700">태그</h2>
					<div class="flex flex-wrap gap-2">
						{#each studyPost.tags as t}
							<span
								class="inline-flex items-center gap-1 rounded-full border border-gray-300 bg-white px-3 py-1 text-sm text-gray-800"
								title={t}
							>
								#{t}
							</span>
						{/each}
					</div>
				</section>
			{/if}
			<!-- 메타 정보 (타일) -->
			<section class="grid gap-4 sm:grid-cols-3">
				<div class="rounded-xl border border-gray-200 bg-white p-4">
					<div class="flex items-center justify-between">
						<span class="inline-flex items-center gap-2 text-sm text-gray-600">
							<Users class="h-4 w-4" /> 최대 인원
						</span>
						<span class="text-base font-semibold text-gray-900">{studyPost.maxPeople}명</span>
					</div>
				</div>

				<div class="rounded-xl border border-gray-200 bg-white p-4">
					<div class="flex items-center justify-between">
						<span class="inline-flex items-center gap-2 text-sm text-gray-600">
							<UserCheck class="h-4 w-4" /> 승인 인원
						</span>
						<span class="text-base font-semibold text-gray-900">{studyPost.acceptedPeople}명</span>
					</div>
				</div>

				<div class="rounded-xl border border-gray-200 bg-white p-4">
					<div class="flex items-center justify-between">
						<span class="inline-flex items-center gap-2 text-sm text-gray-600">
							<Flag class="h-4 w-4" /> 모집 상태
						</span>
						<span
							class={`rounded-full border px-2.5 py-1 text-sm font-medium
              ${
								studyPost.studyStatus === 'RECRUITING'
									? 'border-emerald-200 bg-emerald-50 text-emerald-700'
									: 'border-gray-300 bg-gray-100 text-gray-700'
							}`}
						>
							{studyPost.studyStatus === 'RECRUITING' ? '모집 중' : '모집 종료'}
						</span>
					</div>
				</div>
			</section>

			<!-- 하단 액션: 좌/우 배치 -->
			<div class="mt-8 flex items-center justify-between">
				<!-- 좌측: 작성자 전용 운영 버튼 -->
				<div class="flex items-center gap-2">
					{#if currentUserId === studyPost.userId}
						<button
							class="rounded-full border border-gray-300 bg-white px-3 py-1.5 text-sm text-gray-800 shadow-sm transition hover:bg-gray-50"
							on:click={() => goto(`/studies/${studyPostId}/applications`)}
						>
							👥 지원자 목록
						</button>

						{#if studyPost.studyStatus === 'RECRUITING'}
							<button
								class="rounded-full border border-red-300 bg-white px-3 py-1.5 text-sm text-red-600 shadow-sm transition hover:bg-red-50"
								on:click={handleCloseRecruitment}
							>
								🔒 모집 종료하기
							</button>
						{/if}
					{/if}
				</div>

				<!-- 우측: 수정/삭제 또는 지원/상태 -->
				<div class="flex items-center gap-3">
					{#if currentUserId === studyPost.userId}
						<button
							class="rounded-full border border-gray-300 bg-white px-4 py-2 text-sm text-gray-800 shadow-sm transition hover:bg-gray-50"
							on:click={handleEdit}
						>
							수정하기
						</button>
						<button
							class="rounded-full border border-red-300 bg-white px-4 py-2 text-sm text-red-600 shadow-sm transition hover:bg-red-50"
							on:click={handleDelete}
						>
							삭제하기
						</button>
					{:else if hasApplied}
						<div
							class="flex items-center gap-2 rounded-md border border-gray-200 bg-gray-50 px-4 py-3"
						>
							{#if applicationStatus === 'ACCEPTED'}
								<span class="text-green-600">✔</span>
							{:else if applicationStatus === 'REJECTED'}
								<span class="text-red-600">✖</span>
							{:else}
								<span class="text-blue-600">⏳</span>
							{/if}
							<span class="font-medium text-gray-800">이미 지원한 스터디입니다.</span>
							<span
								class="rounded-full px-2 py-0.5 text-xs font-medium
                {applicationStatus === 'ACCEPTED'
									? 'bg-green-100 text-green-700'
									: applicationStatus === 'REJECTED'
										? 'bg-red-100 text-red-700'
										: 'bg-blue-100 text-blue-700'}"
							>
								{applicationStatus === 'ACCEPTED'
									? '승인됨'
									: applicationStatus === 'REJECTED'
										? '거절됨'
										: '진행중'}
							</span>
						</div>
					{:else if studyPost.studyStatus === 'RECRUITING'}
						<button
							class="rounded-full border border-blue-300 bg-white px-4 py-2 text-sm text-blue-600 shadow-sm transition hover:bg-blue-50"
							on:click={handleApply}
						>
							지원하기
						</button>
					{/if}
				</div>
			</div>
		</div>
	</div>
{/if}
