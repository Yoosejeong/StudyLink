<!-- ✅ src/routes/auth/+page.svelte -->
<script>
  import { goto } from '$app/navigation';

  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL;

  let email = '';
  let password = '';
  let errorMessage = '';

  async function handleLogin(e) {
    e.preventDefault();

    const res = await fetch(`${apiBaseUrl}/api/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
      credentials: 'include'
    });

    if (!res.ok) {
      const data = await res.json();
      errorMessage = data.message || '로그인 실패';
      return;
    }

    const token = res.headers.get('Authorization');
    if (token) {
      localStorage.setItem('accessToken', token);
    }
    goto('/');
  }
</script>

<!-- ✅ 전체 페이지 -->
<div class="min-h-screen flex flex-col bg-gray-50">

  <!-- ✅ 헤더 -->
  <header class="bg-white shadow-sm border-b">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between items-center h-16">
        <div class="flex items-center">
          <h1 class="text-2xl font-bold text-blue-600">StudyLink</h1>
        </div>
        <div class="flex items-center space-x-4">
          <a href="/" class="px-4 py-1 text-sm rounded-full border border-gray-300 bg-white shadow-sm hover:bg-gray-50">
            팀원 모집
          </a>
          <a href="/auth" class="px-4 py-1 text-sm rounded-full bg-blue-600 text-white shadow hover:bg-blue-700">
            로그인
          </a>
        </div>
      </div>
    </div>
  </header>

<!-- ✅ 메인 로그인 섹션 -->
<main class="flex-1 flex items-center justify-center px-4 py-12">
  <div class="w-full max-w-md bg-white p-8 rounded-lg shadow">
    <h1 class="text-2xl font-bold text-center mb-2">로그인</h1>
    <p class="text-center text-gray-600 mb-6">StudyLink 계정으로 로그인하세요</p>

    <form class="space-y-4" on:submit|preventDefault={handleLogin}>
      <div class="space-y-2">
        <label for="email" class="block text-sm font-medium">이메일</label>
        <input
          id="email"
          type="email"
          placeholder="your@email.com"
          required
          bind:value={email}
          class="w-full border border-gray-300 px-3 py-2 rounded"
        />
      </div>

      <div class="space-y-2">
        <label for="password" class="block text-sm font-medium">비밀번호</label>
        <input
          id="password"
          type="password"
          placeholder="비밀번호를 입력하세요"
          required
          bind:value={password}
          class="w-full border border-gray-300 px-3 py-2 rounded"
        />
      </div>

      <button type="submit" class="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700">
        로그인
      </button>

      {#if errorMessage}
        <p class="text-red-500 text-sm">{errorMessage}</p>
      {/if}
    </form>

    <div class="text-center text-sm mt-4">
      <span class="text-gray-600">계정이 없으신가요? </span>
      <a href="/signup" class="text-blue-600 hover:underline font-medium">회원가입</a>
    </div>
  </div>
</main>

  <!-- ✅ 푸터 -->
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
