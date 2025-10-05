import { writable } from 'svelte/store';

const getInitialLoginState = () => {
  if (typeof localStorage !== 'undefined') {
    return !!localStorage.getItem('accessToken');
  }
  return false;
};

export const isLoggedIn = writable<boolean>(getInitialLoginState());

// 전역 로그아웃 함수
export function logout() {
  try {
    localStorage.removeItem('accessToken');
  } catch {}
  isLoggedIn.set(false);
}
