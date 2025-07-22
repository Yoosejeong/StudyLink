export const load = async ({ fetch }) => {
  const baseUrl = import.meta.env.VITE_API_BASE_URL;
  const res = await fetch(`${baseUrl}/api/study-posts`);

  console.log('✅ API 응답 상태:', res.status);

  if (!res.ok) {
    throw new Error('스터디 목록을 불러오는 데 실패했습니다.');
  }

  const studies = await res.json();
  console.log('✅ 받아온 스터디 목록:', studies);
  return { studies };
};