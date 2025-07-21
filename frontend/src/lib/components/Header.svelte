<script lang="ts">
  import { goto } from '$app/navigation';
  import { isLoggedIn } from '$lib/stores/auth';

  export let handleLogin: () => void;
  export let handleLogout: () => Promise<void>;
</script>

<header class="bg-white shadow-sm">
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
    <div class="flex justify-between items-center h-16">
      <div class="flex items-center">
        <a 
          href="/"
          class="text-2xl font-bold text-blue-600 hover:underline"
        >
          StudyLink
        </a>
      </div>
      <div class="flex items-center space-x-2">
        <button 
          class="px-4 py-1 text-sm rounded-full border border-gray-300 bg-white shadow-sm hover:bg-gray-50"
        >
          팀원 모집
        </button>

        {#if $isLoggedIn}
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
