// src/lib/api/fetchWithAuth.ts
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

let reissueInFlight: Promise<string | null> | null = null;
let onReissueFail: (() => void) | null = null;

/** 외부에서 재발급 실패 시 처리 주입할 수 있게 */
export function registerOnReissueFail(handler: () => void) {
  onReissueFail = handler;
}

/** refresh 쿠키로 새 access 발급 (중복 호출 방지) */
async function reissueAccessToken(): Promise<string | null> {
  if (!reissueInFlight) {
    reissueInFlight = (async () => {
      try {
        const res = await fetch(`${API_BASE_URL}/api/reissue`, {
          method: 'POST',
          credentials: 'include',
        });
        if (!res.ok) return null;

        const raw = res.headers.get('Authorization');
        const token = raw?.startsWith('Bearer ') ? raw.slice('Bearer '.length) : raw || null;

        if (token) {
          localStorage.setItem('accessToken', token);
          return token;
        }
        return null;
      } catch {
        return null;
      } finally {
        // 해당 라운드 종료 후 다음 재발급을 위해 초기화
        reissueInFlight = null;
      }
    })();
  }
  return reissueInFlight;
}

/** 인증 필요한 요청용 공통 fetch */
export async function fetchWithAuth(
  path: string,
  init: RequestInit = {},
  retry = true
): Promise<Response> {
  const headers = new Headers(init.headers || {});
  const access = localStorage.getItem('accessToken');

  // ✅ 토큰이 있을 때만 Authorization 세팅
  if (access) headers.set('Authorization', `Bearer ${access}`);

  const first = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers,
  });

  // 401만 재발급 로직, 그 외/재시도 불가면 통과
  if (first.status !== 401 || !retry) return first;

  // 재발급
  const newAccess = await reissueAccessToken();

  if (!newAccess) {
    onReissueFail?.();
    return first;
  }

  // 새 토큰으로 한 번만 재시도
  const retryHeaders = new Headers(init.headers || {});
  retryHeaders.set('Authorization', `Bearer ${newAccess}`);

  return fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: retryHeaders,
  });
}
