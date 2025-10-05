import { error, type Load } from '@sveltejs/kit';
import { http } from '$lib/api/http';

export const ssr = false;

export const load: Load = async ({ params }) => {
  const r = await http.get(`/api/study-posts/${params.id}`);

  if (r.status === 404) throw error(404, '해당 글이 없습니다.');
  if (!r.ok && r.status !== 401 && r.status !== 403) {
    throw error(r.status, '조회 실패');
  }

  // 401/403인 경우: 페이지로 authRequired 플래그만 넘김 (모달에서 처리)
  if (r.status === 401 || r.status === 403) {
    return { studyPost: null, id: params.id, authRequired: true };
  }

  const data = await r.json();
  return { studyPost: data.result, id: params.id, authRequired: false };
};
