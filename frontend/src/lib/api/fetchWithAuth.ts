// src/lib/api/fetchWithAuth.ts
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

// 우리 API 오리진(프로토콜+호스트+포트)
const API_ORIGIN = new URL(API_BASE_URL).origin;

let reissueInFlight: Promise<string | null> | null = null;
let onReissueFail: (() => void) | null = null;

/** 외부에서 재발급 실패 시 처리 주입할 수 있게 */
export function registerOnReissueFail(handler: () => void) {
  onReissueFail = handler;
}

/** 상대/절대 경로를 실제 호출 URL로 변환 */
function buildUrl(path: string): string {
  if (/^https?:\/\//i.test(path)) return path; // 절대 URL은 그대로
  // 슬래시 중복/누락 방지
  const base = API_BASE_URL.replace(/\/+$/, '');
  const p = path.startsWith('/') ? path : `/${path}`;
  return `${base}${p}`;
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

        // 서버가 Authorization 헤더로 새 토큰을 내려주는 계약
        const raw = res.headers.get('Authorization');
        const token = raw?.startsWith('Bearer ')
          ? raw.slice('Bearer '.length)
          : raw || null;

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
  const url = buildUrl(path);
  const targetOrigin = new URL(url).origin;

  // ✅ 우리 API 오리진으로 가는 요청에만 Authorization/재발급 적용
  if (targetOrigin === API_ORIGIN) {
    const headers = new Headers(init.headers || {});
    const access = localStorage.getItem('accessToken');

    if (access && !headers.has('Authorization')) {
      // 저장된 값이 순수 토큰이라면 Bearer 붙여주기
      const value = access.startsWith('Bearer ') ? access : `Bearer ${access}`;
      headers.set('Authorization', value);
    }

    const first = await fetch(url, { ...init, headers });

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
    return fetch(url, { ...init, headers: retryHeaders });
  }

  // ❗️다른 오리진(예: S3 presigned URL) → 토큰/재발급 절대 적용하지 않음
  return fetch(url, init);
}
