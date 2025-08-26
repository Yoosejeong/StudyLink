import { writable, get } from 'svelte/store';
import { http } from '$lib/api/http';

type Entry = { url: string; expMs: number };
const SKEW_MS = 30_000;
const cache = writable<Map<string, Entry>>(new Map());

function selectMissOrStale(keys: string[]): string[] {
  const now = Date.now();
  const m = get(cache);
  const out: string[] = [];
  for (const k of keys) {
    if (!k) continue;
    const e = m.get(k);
    if (!e || e.expMs <= now + SKEW_MS) out.push(k);
  }
  return Array.from(new Set(out));
}

function upsert(items: Array<{ key: string; url: string; expiresAt: string }>) {
  console.log('[presign][UPsert]', items.map(i => ({key: i.key, date: i.expiresAt, url: i.url.slice(0,80)+'...'})));
  const m = get(cache);
  for (const it of items) {
    const expMs = new Date(it.expiresAt).getTime();
    m.set(it.key, { url: it.url, expMs });
  }
  cache.set(m);
}

export function getPresignedUrl(key?: string | null): string | undefined {
  if (!key) return undefined;
  const e = get(cache).get(key);
  if (!e) return undefined;
  if (Date.now() + SKEW_MS >= e.expMs) return undefined;
  return e.url;
}

export async function ensurePresignedUrls(keys: (string | null | undefined)[]) {
  const cleaned = Array.from(
    new Set(keys.filter((k): k is string => !!k && k.trim().length > 0))
  );

  const need = selectMissOrStale(cleaned);
  if (need.length > 0) {
    const res = await http.post('/api/s3/presign/objects', { keys: need });
    if (res.ok) {
      const data = await res.json();
      const items =
        (data?.result as Array<{ key: string; url: string; expiresAt: string }>) ?? [];
      upsert(items);
    } else {
      // 실패해도 목록 렌더는 진행(이미지 없으면 fallback)
      console.warn('presign batch 실패', res.status);
    }
  }

  const m = get(cache);
  const out = new Map<string, string>();
  const now = Date.now();
  for (const k of cleaned) {
    const e = m.get(k);
    if (e && e.expMs > now + SKEW_MS) out.set(k, e.url);
  }
  return out;
}

export function invalidatePresigned(key: string) {
  const m = get(cache);
  m.delete(key);
  cache.set(m);
}
