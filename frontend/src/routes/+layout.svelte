<script lang="ts">
  import "../app.css";
  import Header from '$lib/components/Header.svelte';
  import Footer from '$lib/components/Footer.svelte';
  import { goto } from '$app/navigation';
  import { isLoggedIn, currentUser } from '$lib/stores/auth'; 
  import { onMount } from 'svelte';
  import { http } from '$lib/api/http';

  onMount(() => {
    const token = localStorage.getItem('accessToken');
    isLoggedIn.set(!!token); 
  });

  $: {
    if ($isLoggedIn && !$currentUser) {
      http.get('/api/users').then(async res => {
        if (res.ok) {
          const data = await res.json();
          currentUser.set(data.result);
        } else if (res.status === 401 || res.status === 403) {
          isLoggedIn.set(false);
          currentUser.set(null);
        }
      }).catch(err => {
        console.error('전역 유저 정보 조회 실패:', err);
      });
    } else if (!$isLoggedIn && $currentUser) {
      currentUser.set(null);
    }
  }

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