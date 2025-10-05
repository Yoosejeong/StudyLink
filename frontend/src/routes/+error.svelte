<!-- src/routes/+error.svelte -->
<script lang="ts">
  import { page } from '$app/stores';
  export let status: number | undefined;
  export let error: unknown;

  // fallback: 라우트에서 안 넘겨줄 때 page 스토어 값 사용
  $: s = status ?? $page.status;
  $: e = (error ?? $page.error) as { message?: string } | undefined;

  // 메시지 fallback
  $: msg = e?.message ?? '문제가 발생했어요.';
</script>

<svelte:head>
  <title>{s === 404 ? '페이지를 찾을 수 없어요' : `오류 (${s})`}</title>
</svelte:head>

<div class="min-h-[70vh] flex items-center justify-center px-6">
  {#if s === 404}
    <div class="text-center">
      <div class="text-7xl font-extrabold">404</div>
      <p class="mt-3 text-lg text-gray-600">요청하신 페이지를 찾을 수 없어요.</p>
      <div class="mt-6 flex items-center justify-center gap-3">
        <a href="/" class="rounded-lg bg-blue-600 px-4 py-2 text-white hover:bg-blue-700">홈으로</a>
      </div>
    </div>
  {:else}
    <div class="text-center">
      <div class="text-7xl font-extrabold">{s}</div>
      <p class="mt-3 text-lg text-gray-600">{msg}</p>
      <div class="mt-6">
        <a href="/" class="rounded-lg bg-blue-600 px-4 py-2 text-white hover:bg-blue-700">홈으로</a>
      </div>
    </div>
  {/if}
</div>
