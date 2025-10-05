<script lang="ts">
  import "../app.css";
  import Header from '$lib/components/Header.svelte';
  import Footer from '$lib/components/Footer.svelte';
  import { goto } from '$app/navigation';
  import { isLoggedIn } from '$lib/stores/auth'; 
  import { onMount } from 'svelte';

  onMount(() => {
  const token = localStorage.getItem('accessToken');
  isLoggedIn.set(!!token); 
});
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL;

  function handleLogin() {
    goto('/auth');
  }

  async function handleLogout() {
    try {
      const res = await fetch(`${apiBaseUrl}/api/logout`, {
        method: 'POST',
        credentials: 'include'
      });
      if (!res.ok) {
        alert('로그아웃 실패!');
        return;
      }
      localStorage.removeItem('accessToken');
      isLoggedIn.set(false); 
      alert('로그아웃 완료!');
      goto('/');
    } catch (err) {
      alert('네트워크 오류!');
    }
  }
</script>

<div class="min-h-screen flex flex-col">
  <Header {handleLogin} {handleLogout} /> <!-- isLoggedIn은 store 내부에서 구독 -->
  <main class="flex-1">
    <slot />
  </main>
  <Footer />
</div>