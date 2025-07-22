<script lang="ts">
  import { User } from 'lucide-svelte';
  export let data;
  console.log('✅ data:', data);

  interface Study {
    studyPostId: number;
    title: string;
    nickname: string;
    maxPeople: number;
    acceptedPeople: number;
    studyStatus: 'RECRUITING' | 'CLOSED';
    updatedAt: string;
  }

  const studies: Study[] = data.studies?.result?.content ?? [];
</script>

  <!-- ✅ 메인 콘텐츠 -->
  <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
    <!-- 히어로 섹션 -->
    <div class="bg-gradient-to-r from-yellow-300 to-yellow-400 rounded-3xl mx-4 mb-16 overflow-hidden">
      <div class="relative px-8 py-12 md:px-16 md:py-16">
        <div class="flex flex-col lg:flex-row items-center justify-between">
          <!-- 왼쪽 콘텐츠 -->
          <div class="flex-1 mb-8 lg:mb-0">
            <div class="inline-block bg-pink-400 text-white px-4 py-2 rounded-full text-sm font-medium mb-4">
              JOIN US
            </div>
            <h2 class="text-4xl md:text-5xl font-bold text-gray-900 mb-4">Grow Together! 🤍</h2>
            <p class="text-lg md:text-xl text-gray-700 max-w-lg">원하는 스터디에 지원하고, 함께 성장하세요!</p>
          </div>

          <!-- 오른쪽 채팅 UI -->
          <div class="flex-1 relative max-w-md">
            <div class="absolute -top-4 -right-4 bg-white rounded-full p-3 shadow-lg">
              <div class="w-8 h-8 bg-purple-500 rounded-full flex items-center justify-center">📢</div>
            </div>
            <div class="space-y-4">
              <div class="bg-white rounded-2xl p-4 shadow-md ml-8">
                <p class="font-medium text-gray-800">스터디를 모집하고 지원해요! 📚</p>
              </div>
              <div class="bg-white rounded-2xl p-4 shadow-md mr-8">
                <p class="text-gray-700">StudyLink에서</p>
                <p class="text-gray-700">함께 성장할 팀원을 찾아보세요 🚀</p>
              </div>
              <div class="bg-green-400 text-white rounded-2xl p-4 shadow-md ml-4">
                <p>매일 새로운 스터디</p>
                <p>모집글을 확인하세요 💚</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 스터디 카드 그리드 -->
  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
    {#each studies as study (study.studyPostId)}
      <div class="border rounded-lg p-4 hover:shadow-lg transition-shadow cursor-pointer">
        <div class="mb-3">
          <span class="inline-block bg-yellow-100 text-yellow-800 px-2 py-1 rounded text-xs">
            {study.studyStatus}
          </span>
        </div>
        <h3 class="font-semibold text-gray-900 mb-2 line-clamp-2 leading-tight">
          {study.title}
        </h3>
        <div class="flex items-center mb-1 text-sm text-gray-600">
          <User class="w-4 h-4 text-blue-600" />
          <span class="ml-1 font-medium">{study.nickname}</span>
        </div>
        <div class="text-sm text-gray-500">
          인원 {study.acceptedPeople} / {study.maxPeople}
        </div>
      </div>
    {/each}
  </div>
</main>