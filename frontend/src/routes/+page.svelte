<script lang="ts">
  import { goto } from '$app/navigation';
  import { User } from 'lucide-svelte';
  import { onMount } from 'svelte';

  // ✅ 로그인 상태 변수
  let isLoggedIn = false;

  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL;

  // ✅ 페이지 진입 시 accessToken 있으면 로그인 상태 true
  onMount(() => {
    const token = localStorage.getItem('accessToken');
    isLoggedIn = !!token;
  });

  // ✅ 로그인 버튼 클릭 시
  function handleLogin() {
    goto('/auth'); // 로그인 페이지로 이동
  }

    // ✅ 로그아웃 함수
  async function handleLogout() {
    try {
      const res = await fetch(`${apiBaseUrl}/api/logout`, {
        method: 'POST',
        credentials: 'include' // ✅ refresh 쿠키 보내려면 필수!
      });

      if (!res.ok) {
        console.error('🚫 로그아웃 실패:', await res.text());
        alert('로그아웃 실패! 다시 시도해주세요.');
        return;
      }

      // ✅ 서버가 Refresh Token DB에서 삭제 + 쿠키 만료
      // 클라이언트는 Access Token만 지우면 됨
      localStorage.removeItem('accessToken');

      // ✅ 상태 갱신해서 버튼 바꿔주기
      isLoggedIn = false;

      alert('로그아웃 완료!');
      goto('/'); // 필요하면 메인으로 이동
    } catch (err) {
      console.error('🚫 로그아웃 에러:', err);
      alert('네트워크 오류! 다시 시도해주세요.');
    }
  }

  const studies = [
    {
      id: 1,
      title: "프론트엔드 스터디 팀원 모집합니다",
      author: "프론트마스터",
      tags: ["스터디"],
    },
    {
      id: 2,
      title: "React & TypeScript 스터디 함께해요",
      author: "타입러버",
      tags: ["스터디"],
    },
    {
      id: 3,
      title: "백엔드 개발 스터디 (Spring Boot)",
      author: "백엔드지존",
      tags: ["스터디"],
    },
    {
      id: 4,
      title: "알고리즘 코딩테스트 스터디 모집",
      author: "알고리즘왕",
      tags: ["스터디"],
    },
  ];
</script>

<div class="min-h-screen bg-gray-50">
  <!-- ✅ 헤더 -->
  <header class="bg-white shadow-sm">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between items-center h-16">
        <div class="flex items-center">
          <h1 class="text-2xl font-bold text-blue-600">StudyLink</h1>
        </div>
        <div class="flex items-center space-x-2">
          <button class="px-4 py-1 text-sm rounded-full border border-gray-300 bg-white shadow-sm hover:bg-gray-50">
            팀원 모집
          </button>

          <!-- ✅ 로그인 상태에 따라 버튼 변경 -->
          {#if isLoggedIn}
            <button
              class="px-4 py-1 text-sm rounded-full bg-gray-600 text-white shadow hover:bg-gray-700"
              on:click={handleLogout}
            >
              로그아웃
            </button>
          {:else}
            <button
              class="px-4 py-1 text-sm rounded-full bg-blue-600 text-white shadow hover:bg-blue-700"
              on:click={handleLogin}
            >
              로그인
            </button>
          {/if}
        </div>
      </div>
    </div>
  </header>

  <!-- ✅ 메인 콘텐츠 -->
  <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
    <!-- 히어로 섹션 -->
    <div class="bg-gradient-to-r from-yellow-300 to-yellow-400 rounded-3xl mx-4 mb-16 overflow-hidden">
      <div class="relative px-8 py-12 md:px-16 md:py-16">
        <div class="flex flex-col lg:flex-row items-center justify-between">
          <!-- 왼쪽 콘텐츠 -->
          <div class="flex-1 mb-8 lg:mb-0">
            <div class="inline-block bg-pink-400 text-white px-4 py-2 rounded-full text-sm font-medium mb-4">
              JOIN US
            </div>
            <h2 class="text-4xl md:text-5xl font-bold text-gray-900 mb-4">Grow Together! 🤍</h2>
            <p class="text-lg md:text-xl text-gray-700 max-w-lg">원하는 스터디에 지원하고, 함께 성장하세요!</p>
          </div>

          <!-- 오른쪽 채팅 UI -->
          <div class="flex-1 relative max-w-md">
            <div class="absolute -top-4 -right-4 bg-white rounded-full p-3 shadow-lg">
              <div class="w-8 h-8 bg-purple-500 rounded-full flex items-center justify-center">📢</div>
            </div>
            <div class="space-y-4">
              <div class="bg-white rounded-2xl p-4 shadow-md ml-8">
                <p class="font-medium text-gray-800">스터디를 모집하고 지원해요! 📚</p>
              </div>
              <div class="bg-white rounded-2xl p-4 shadow-md mr-8">
                <p class="text-gray-700">StudyLink에서</p>
                <p class="text-gray-700">함께 성장할 팀원을 찾아보세요 🚀</p>
              </div>
              <div class="bg-green-400 text-white rounded-2xl p-4 shadow-md ml-4">
                <p>매일 새로운 스터디</p>
                <p>모집글을 확인하세요 💚</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 스터디 카드 그리드 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
      {#each studies as study (study.id)}
        <div class="border rounded-lg p-4 hover:shadow-lg transition-shadow cursor-pointer">
          <div class="mb-3">
            <span class="inline-block bg-yellow-100 text-yellow-800 px-2 py-1 rounded text-xs">
              {study.tags[0]}
            </span>
          </div>
          <h3 class="font-semibold text-gray-900 mb-2 line-clamp-2 leading-tight">
            {study.title}
          </h3>
          <div class="flex items-center">
            <User class="w-4 h-4 text-blue-600" />
            <span class="ml-1 text-sm font-medium text-gray-700">{study.author}</span>
          </div>
        </div>
      {/each}
    </div>
  </main>

  <!-- 푸터 -->
  <footer class="bg-white border-t mt-20">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="text-center space-y-2">
        <p class="text-gray-600">
          Contact us:
          <a href="mailto:nived3@naver.com" class="text-blue-600 hover:underline">
            nived3@naver.com
          </a>
        </p>
        <p class="text-gray-500">함께 성장하는 스터디 플랫폼 © 2025 StudyLink</p>
      </div>
    </div>
  </footer>
</div>
