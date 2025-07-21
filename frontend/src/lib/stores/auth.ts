import { writable } from 'svelte/store';

const getInitialLoginState = () => {
  if (typeof localStorage !== 'undefined') {
    return !!localStorage.getItem('accessToken');
  }
  return false;
};

export const isLoggedIn = writable<boolean>(getInitialLoginState());