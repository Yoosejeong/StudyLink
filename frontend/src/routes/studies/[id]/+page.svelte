<!-- src/routes/studies/[id]/+page.svelte -->
<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { http } from '$lib/api/http';
	import { clickOutside } from '$lib/actions/clickOutside';
	import {
		ArrowLeft,
		Users,
		UserCheck,
		CheckCircle,
		XCircle,
		Clock,
		MoreVertical
	} from 'lucide-svelte';
	import { slide } from 'svelte/transition';

	const DEFAULT_AVATAR = '/avatars/default.jpg';

	// ✅ load(+page.ts)에서 넘어오는 데이터
	export let data: { studyPost: StudyPost; id: string; authRequired?: boolean };

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
		profileUrl?: string | null;
		createdAt: string;
		updatedAt: string;
	};

	type CurrentUser = {
		userId: number;
		nickname: string;
	};

	// ✅ load에서 받은 값으로 고정
	const studyPost = data.studyPost;
	const studyPostId = data.id;

	// 클라이언트에서만 필요한 상태들
	let currentUserId: number | null = null;
	let currentUserNickname: string | null = null;

	let hasApplied = false;
	let applicationStatus: 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'CANCELED' | null = null;
	let currentApplicationId: number | null = null; // 

	let isMenuOpen = false;

	// 🔶 로그인 유도 모달 상태/핸들러
	let showAuthModal = false;
	function openAuthModal() {
		showAuthModal = true;
	}
	function closeAuthModal() {
		showAuthModal = false;
	}
	function goAuth() {
		showAuthModal = false;
		goto('/auth');
	}

	// 페이지 진입 시 내 정보 / 지원 여부 조회
	onMount(async () => {
		// load에서 401/403이 나왔을 때: 모달 띄우고 종료
		if (data.authRequired) {
			openAuthModal();
			return;
		}
		await Promise.all([fetchUser(), checkApplicationStatus()]);
	});

	function handleEdit() {
		isMenuOpen = false;
		goto(`/studies/${studyPostId}/edit`);
	}

	// 🔶 공통 권한 가드
	function redirectOnAuthError(res: Response): boolean {
		if (res.status === 401 || res.status === 403) {
			openAuthModal();
			return true;
		}
		return false;
	}

	async function fetchUser() {
		try {
			const res = await http.get('/api/users');
			if (redirectOnAuthError(res)) return;
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
			if (redirectOnAuthError(res)) return;
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
			if (redirectOnAuthError(res)) return;
			if (res.ok) {
				const data = await res.json();
				hasApplied = data.result.hasApplied;
				currentApplicationId = data.result.applicationId || null;
				applicationStatus = data.result.applicationStatus;
				currentApplicationId = data.result.studyApplicationId || null;
			} else {
				console.error('지원 여부 확인 실패', res.status);
			}
		} catch (err) {
			console.error('지원 여부 요청 오류', err);
		}
	}

	async function handleCancel() {
		if (!currentApplicationId) {
			alert('오류: 지원서 ID를 찾을 수 없습니다. ');
			return;
		}

		const confirmed = confirm('정말로 지원을 취소하시겠습니까?');
		if (!confirmed) return;

		try {
			const res = await http.patch(`/api/study-applications/${currentApplicationId}/cancel`);
			if (redirectOnAuthError(res)) return;

			if (res.ok) {
				alert('지원이 취소되었습니다.');
				// 상태를 다시 체크해서 (CANCELED로) UI를 업데이트합니다.
				// ('다시 지원하기' 버튼이 보이게 됩니다)
				await checkApplicationStatus();
			} else {
				const errorData = await safeJson(res);
				// 409 Conflict (낙관적 락 충돌)
				if (res.status === 409) {
					alert(
						'요청 처리 중 충돌이 발생했습니다. (작성자가 방금 승인/거절했을 수 있습니다) 페이지를 새로고침합니다.'
					);
					window.location.reload();
				} else {
					alert(`취소 실패: ${errorData?.message || res.status}`);
				}
			}
		} catch (err) {
			console.error('취소 요청 오류', err);
			alert('네트워크 오류로 취소에 실패했습니다.');
		}
	}

	async function handleDelete() {
		isMenuOpen = false;
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
		isMenuOpen = false;
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
			// 즉시 화면 반영
			studyPost.studyStatus = 'CLOSED';
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

	function closeMenu() {
		isMenuOpen = false;
	}

	// 파생값
	$: categoryLabel = studyPost
		? typeof studyPost.category === 'object'
			? studyPost.category.label
			: (CAT_LABEL[studyPost.category as CategoryCode] ?? (studyPost.category as string))
		: '';

	$: remainingSeats = studyPost ? studyPost.maxPeople - studyPost.acceptedPeople : 0;
</script>

{#if showAuthModal}
	<!-- 🔶 로그인 안내 모달 -->
	<div class="fixed inset-0 z-50 flex items-center justify-center">
		<div class="absolute inset-0 bg-black/40"></div>
		<div class="relative w-full max-w-sm rounded-2xl bg-white p-6 shadow-xl">
			<h3 class="mb-2 text-lg font-bold text-gray-900">로그인이 필요합니다</h3>
			<p class="mb-5 text-sm text-gray-600">해당 기능을 사용하려면 로그인이 필요해요.</p>
			<div class="flex justify-end gap-2">
				<button
					class="rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700"
					on:click={goAuth}
				>
					로그인하기
				</button>
			</div>
		</div>
	</div>
{/if}

{#if studyPost}
	<div class="mx-auto max-w-3xl px-4 py-12 font-sans" use:clickOutside={closeMenu}>
		<div class="rounded-2xl bg-white p-6 shadow-lg sm:p-8">
			<!-- 뒤로가기 버튼 -->
			<div class="mb-4">
				<button
					type="button"
					class="inline-flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-800"
					on:click={() => goto('/')}
				>
					<ArrowLeft class="h-4 w-4" />
					<span>목록으로</span>
				</button>
			</div>

			<!-- 헤더: 카테고리, 제목, 작성자 메뉴 -->
			<header class="relative mb-6">
				<div class="mb-3">
					<span class="rounded bg-indigo-100 px-2.5 py-1 text-xs font-semibold text-indigo-800">
						{categoryLabel}
					</span>
				</div>
				<h1 class="mb-3 text-3xl font-bold text-gray-900 sm:text-4xl">
					{studyPost.title}
				</h1>
				<!-- 아바타 + 닉네임 + 날짜 -->
				<div class="flex items-center justify-between text-sm text-gray-500">
					<div class="flex items-center gap-2">
						<img
							src={studyPost.profileUrl ?? DEFAULT_AVATAR}
							alt="작성자 프로필"
							class="h-7 w-7 rounded-full object-cover"
							loading="lazy"
							decoding="async"
						/>
						<span class="font-medium text-gray-700">{studyPost.nickname}</span>
						<span class="mx-2 text-gray-400">·</span>
						<span>{new Date(studyPost.createdAt).toLocaleDateString()}</span>
					</div>
				</div>

				<!-- 작성자 메뉴 -->
				{#if currentUserId === studyPost.userId}
					<div class="absolute top-0 right-0">
						<button
							on:click|stopPropagation={() => (isMenuOpen = !isMenuOpen)}
							class="rounded-full p-2 text-gray-500 hover:bg-gray-100 hover:text-gray-800"
							aria-label="게시물 관리 메뉴"
						>
							<MoreVertical class="h-5 w-5" />
						</button>
						{#if isMenuOpen}
							<div
								transition:slide={{ duration: 150 }}
								class="ring-opacity-5 absolute right-0 mt-2 w-40 origin-top-right rounded-md bg-white py-1 shadow-lg ring-1 ring-black focus:outline-none"
							>
								{#if studyPost.studyStatus === 'RECRUITING'}
									<button
										on:click={handleCloseRecruitment}
										class="block w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100"
									>
										🔒 모집 종료
									</button>
								{/if}
								<button
									on:click={handleEdit}
									class="block w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100"
								>
									✏️ 수정
								</button>
								<button
									on:click={handleDelete}
									class="block w-full px-4 py-2 text-left text-sm text-red-600 hover:bg-red-50"
								>
									🗑️ 삭제
								</button>
							</div>
						{/if}
					</div>
				{/if}
			</header>

			<hr class="my-6 border-gray-200" />

			<!-- 스터디 소개 -->
			<section class="mb-8">
				<h2 class="mb-3 text-lg font-semibold text-gray-800">스터디 소개</h2>
				<div
					class="prose prose-sm max-w-none rounded-lg border border-gray-200 bg-gray-50/50 p-5 leading-relaxed whitespace-pre-wrap text-gray-700"
				>
					{studyPost.content}
				</div>
			</section>

			<!-- 태그 -->
			{#if studyPost.tags && studyPost.tags.length > 0}
				<section class="mb-8">
					<div class="flex flex-wrap gap-2">
						{#each studyPost.tags as tag}
							<span class="rounded-full bg-gray-100 px-3 py-1 text-sm font-medium text-gray-600">
								#{tag}
							</span>
						{/each}
					</div>
				</section>
			{/if}

			<!-- 모집 현황 -->
			<div class="my-8 rounded-lg border border-gray-200 bg-white p-5">
				<div class="flex items-center justify-between">
					<div class="flex items-center gap-6 text-gray-600">
						<div class="flex items-center gap-2">
							<Users class="h-5 w-5" />
							<span>모집인원 <strong class="text-gray-900">{studyPost.maxPeople}</strong>명</span>
						</div>
						<div class="flex items-center gap-2">
							<UserCheck class="h-5 w-5" />
							<span
								>현재 승인인원 <strong class="text-gray-900">{studyPost.acceptedPeople}</strong
								>명</span
							>
						</div>
					</div>
					<div class="text-right">
						<span class="text-gray-600">남은 자리 </span>
						<strong class="text-lg font-bold text-emerald-600">{remainingSeats}</strong><span
							class="text-emerald-600">명</span
						>
					</div>
				</div>
			</div>

			<!-- 하단 액션 -->
			<div class="mt-8">
				{#if currentUserId === studyPost.userId}
					<button
						on:click={() => goto(`/studies/${studyPostId}/applications`)}
						class="flex w-full items-center justify-center gap-2 rounded-lg bg-gray-800 px-5 py-4 text-base font-bold text-white shadow-md transition hover:bg-gray-900 focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 focus:outline-none"
					>
						<Users class="h-5 w-5" />
						<span>지원자 목록 보기</span>
					</button>
				{:else if studyPost.studyStatus === 'RECRUITING' && (!hasApplied || applicationStatus === 'CANCELED')}
					<button
						on:click={handleApply}
						class="w-full rounded-lg bg-blue-600 px-5 py-4 text-base font-bold text-white shadow-md transition hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none"
					>
						{applicationStatus === 'CANCELED' ? '다시 지원하기' : '스터디 지원하기'}
					</button>
				{:else if hasApplied}
					{#if applicationStatus === 'PENDING'}
						<div class="flex flex-col gap-3">
							<div
								class="flex w-full items-center justify-center gap-3 rounded-lg border border-blue-200 bg-gray-50 px-5 py-4 text-base font-semibold text-blue-700"
							>
								<Clock class="h-6 w-6" />
								<span>지원서 검토 결과를 기다리는 중</span>
							</div>

							<button
								on:click={handleCancel}
								class="w-full rounded-lg border border-gray-300 bg-white px-5 py-4 text-base font-bold text-gray-700 shadow-sm transition hover:bg-gray-50 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none"
							>
								지원 취소하기
							</button>
						</div>
					{:else}
						{@const statusInfo = {
							ACCEPTED: { text: '스터디에 참여중입니다', icon: CheckCircle, color: 'green' },
							REJECTED: { text: '아쉽지만, 참여가 거절되었습니다', icon: XCircle, color: 'red' }
						}[applicationStatus]}

						{#if statusInfo}
							<div
								class={`flex w-full items-center justify-center gap-3 rounded-lg border bg-gray-50 px-5 py-4 text-base font-semibold
						${
							statusInfo.color === 'green'
								? 'border-green-200 text-green-700'
								: 'border-red-200 text-red-700'
						}`}
							>
								<svelte:component this={statusInfo.icon} class="h-6 w-6" />
								<span>{statusInfo.text}</span>
							</div>
						{/if}
					{/if}
				{:else}
					<div
						class="w-full cursor-not-allowed rounded-lg bg-gray-200 px-5 py-4 text-center text-base font-bold text-gray-500"
					>
						모집이 마감되었습니다
					</div>
				{/if}
			</div>
		</div>
	</div>
{:else if !showAuthModal}
	<!-- 글 데이터가 없고 모달도 안 보이는 경우(예: 일시적 오류) -->
	<div class="mx-auto max-w-3xl px-4 py-24 text-center text-gray-500">
		데이터를 불러오지 못했습니다.
	</div>
{/if}
