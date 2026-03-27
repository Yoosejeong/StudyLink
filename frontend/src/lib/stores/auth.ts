import { writable } from 'svelte/store';

export type CurrentUser = {
  userId: number;
  nickname: string;
  email?: string;
  username?: string;
  profileKey?: string | null;
  profileUrl?: string | null;
  walletBalance?: number;
};

const getInitialLoginState = () => {
  if (typeof localStorage !== 'undefined') {
    return !!localStorage.getItem('accessToken');
  }
  return false;
};

export const isLoggedIn = writable<boolean>(getInitialLoginState());
export const currentUser = writable<CurrentUser | null>(null);

// 전역 로그아웃 함수
export function logout() {
  try {
    localStorage.removeItem('accessToken');
  } catch {}
  isLoggedIn.set(false);
  currentUser.set(null);
}
