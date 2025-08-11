// src/lib/api/http.ts
import { fetchWithAuth } from './fetchWithAuth';

function jsonInit(method: string, body?: unknown, init: RequestInit = {}): RequestInit {
  const headers = new Headers(init.headers || {});
  if (!headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  return {
    ...init,
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  };
}

export const http = {
  get: (path: string, init: RequestInit = {}) =>
    fetchWithAuth(path, { ...init, method: 'GET' }),

  post: (path: string, body?: unknown, init: RequestInit = {}) =>
    fetchWithAuth(path, jsonInit('POST', body, init)),

  put: (path: string, body?: unknown, init: RequestInit = {}) =>
    fetchWithAuth(path, jsonInit('PUT', body, init)),

  patch: (path: string, body?: unknown, init: RequestInit = {}) =>
    fetchWithAuth(path, jsonInit('PATCH', body, init)),

  delete: (path: string, init: RequestInit = {}) =>
    fetchWithAuth(path, { ...init, method: 'DELETE' }),
};
