import { writable } from 'svelte/store';

export const avatarVersion = writable(0);

/** 프로필 이미지가 갱신됐음을 전체에 알림 */
export function bumpAvatar() {
  avatarVersion.update((n) => n + 1);
}