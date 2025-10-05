<!-- ✅ src/routes/auth/+page.svelte -->
<script lang="ts">
  import { goto } from '$app/navigation';
  import { isLoggedIn } from '$lib/stores/auth';

  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL;
  let email = '';
  let password = '';
  let errorMessage = '';

  async function handleLogin() {
    const res = await fetch(`${apiBaseUrl}/api/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',              
      body: JSON.stringify({ email, password })
    });

    if (!res.ok) {
      const data = await res.json().catch(() => ({}));
      errorMessage = data.message || '로그인 실패';
      return;
    }

    const raw = res.headers.get('Authorization');
    const token = raw?.startsWith('Bearer ') ? raw.slice('Bearer '.length) : raw;
    if (token) {
      localStorage.setItem('accessToken', token);
      isLoggedIn.set(true);
    }

    goto('/');
  }
</script>

<main class="flex flex-1 flex-col items-center justify-center px-4 py-12 bg-gray-50">
  <div class="w-full max-w-md bg-white p-8 rounded-lg shadow">
    <h1 class="text-2xl font-bold text-center mb-2">로그인</h1>
    <p class="text-center text-gray-600 mb-6">StudyLink 계정으로 로그인하세요</p>

    <form class="space-y-4" on:submit|preventDefault={handleLogin}>
      <div class="space-y-2">
        <label for="email" class="block text-sm font-medium">이메일</label>
        <input
          id="email"
          type="email"
          required
          placeholder="your@email.com"
          bind:value={email}
          class="w-full border border-gray-300 px-3 py-2 rounded"
        />
      </div>
      <div class="space-y-2">
        <label for="password" class="block text-sm font-medium">비밀번호</label>
        <input
          id="password"
          type="password"
          required
          placeholder="비밀번호를 입력하세요"
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
