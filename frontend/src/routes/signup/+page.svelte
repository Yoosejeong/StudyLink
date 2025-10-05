<script lang="ts">
  import { goto } from '$app/navigation';

  // ✅ 상태 변수
  let username = '';
  let nickname = '';
  let email = '';
  let password = '';
  let errorMessage = '';

  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL;

  async function handleSignup(e: Event) {
    e.preventDefault();

    const response = await fetch(`${apiBaseUrl}/api/signUp`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, nickname, email, password })
    });

    if (!response.ok) {
      const data = await response.json();
      errorMessage = data.message || '회원가입 실패';
      return;
    }

    goto('/auth'); // 가입 성공하면 로그인 페이지로 이동
  }
</script>

<!-- ✅ 전체 페이지 -->
<div class="min-h-screen flex flex-col bg-gray-50">

  <!-- ✅ 메인 회원가입 폼 -->
  <main class="flex-1 flex items-center justify-center px-4 py-12">
    <div class="w-full max-w-md bg-white p-8 rounded-lg shadow">
      <h1 class="text-2xl font-bold text-center mb-2">회원가입</h1>
      <p class="text-center text-gray-600 mb-6">StudyLink에 가입하여 함께 성장해보세요</p>

      <form class="space-y-4" on:submit|preventDefault={handleSignup}>
        <div class="space-y-2">
          <label for="username" class="block text-sm font-medium">회원이름</label>
          <input
            id="username"
            type="text"
            placeholder="실명을 입력하세요"
            required
            bind:value={username}
            class="w-full border border-gray-300 px-3 py-2 rounded"
          />
        </div>

        <div class="space-y-2">
          <label for="nickname" class="block text-sm font-medium">닉네임</label>
          <input
            id="nickname"
            type="text"
            placeholder="사용할 닉네임을 입력하세요"
            required
            bind:value={nickname}
            class="w-full border border-gray-300 px-3 py-2 rounded"
          />
        </div>

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
          회원가입
        </button>

        {#if errorMessage}
          <p class="text-red-500 text-sm">{errorMessage}</p>
        {/if}
      </form>

      <div class="text-center text-sm mt-4">
        <span class="text-gray-600">이미 계정이 있으신가요? </span>
        <a href="/auth" class="text-blue-600 hover:underline font-medium">로그인</a>
      </div>
    </div>
  </main>

</div>
