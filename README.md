# 🚀 StudyLink: 스터디 그룹 매칭 및 관리 플랫폼

StudyLink는 스터디 그룹을 찾거나 팀원을 모집하는 과정을 더 쉽고 효율적으로 만들기 위해 기획된 웹 애플레이션입니다. 

<br>

**🔗 Live Demo:** [https://www.studylink.site](https://www.studylink.site)

<!--  **📚 API Docs:** [API 명세서 바로가기](https://api.studylink.site/swagger-ui/index.html) -->

<br>

## 🎯주요 기능

<details>
<summary><strong>✅ 1. 메인 화면: 스터디 목록 조회 및 필터링</strong></summary>
<br>
전체 스터디 목록을 카드 형태로 보여주며, 모집 상태별 필터를 제공합니다.
<br><br>
<img width="2662" height="1710" alt="image" src="https://github.com/user-attachments/assets/23fa930f-ac39-411d-bdee-9ec9344514b3" />
</details>

<details>
<summary><strong>🔍 2. 상세 화면: 스터디 정보 확인 및 지원/지원자 확인</strong></summary>
<br>
게시글의 상세 내용을 확인하고 스터디 지원과 관련된 모든 활동을 할 수 있는 페이지입니다. 
사용자의 권한(작성자/지원자)에 따라 보이는 화면이 달라집니다.
<br><br>
<table>
  <tr>
    <td align="center"><strong>작성자 화면</strong></td>
    <td align="center"><strong>스터디 지원 화면</strong></td>
    <td align="center"><strong>지원 후 상태 확인</strong></td>
  </tr>
  <tr>
    <td><img width="400" alt="작성자 화면" src="https://github.com/user-attachments/assets/c1208d3b-e8eb-4046-818d-0ecaad7542d3" /></td>
    <td><img width="400" alt="스터디 지원 화면" src="https://github.com/user-attachments/assets/57df4131-feb1-475c-8966-f720bb616ec8" /></td>
    <td><img width="400" alt="지원 후 상태 확인" src="https://github.com/user-attachments/assets/5c4f8d01-8fe3-45ed-9d66-7247d77b8fb6" /></td>
  </tr>
  <tr>
    <td align="center">게시글 수정/삭제, 모집 종료로 <br>게시글을 관리할 수 있습니다.</td>
    <td align="center">사용자가 스터디에<br>지원을 신청하는 화면입니다.</td>
    <td align="center">지원을 완료한 사용자는<br>자신의 지원 상태를 확인할 수 있습니다.</td>
  </tr>
</table>
</details>
<details>
<summary><strong>🤝 3. 지원자 목록 조회 및 관리</strong></summary>
<br>
스터디 작성자는 자신의 게시글에 지원한 사용자들의 목록을 확인하고, 지원을 수락하거나 거절하며 팀원 모집 과정을 직접 관리할 수 있습니다.
<br><br>
<img width="2448" height="584" alt="image" src="https://github.com/user-attachments/assets/e025278d-00b3-48f2-a92a-f11d1eb51bb4" />

</details>

<details>
<summary><strong>📄 4. 나의 지원 목록</strong></summary>
<br>
사용자는 자신이 어떤 스터디에 지원했는지 목록을 한눈에 볼 수 있으며, 지원 상태(대기, 승인, 거절)를 실시간으로 확인할 수 있습니다.
<br><br>
<img width="2554" height="870" alt="image" src="https://github.com/user-attachments/assets/c3bfa71b-7b0e-4b7a-ab36-8a36132e34d0" />
</details>

<details>
<summary><strong>✍️ 5. 글 작성 및 수정</strong></summary>
<br>
스터디 모집 게시글을 작성합니다. 
<img width="1606" height="1670" alt="image" src="https://github.com/user-attachments/assets/85b1957d-42bc-4c26-8165-a1b38f0ba8ae" />
</details>

<details>
<summary><strong>👤 6. 프로필 편집</strong></summary>
<br>
사용자는 자신의 프로필 정보를 수정할 수 있습니다.
<br><br>
<img width="726" height="699" alt="스크린샷 2025-10-16 오후 5 38 22" src="https://github.com/user-attachments/assets/1a0382b9-15e3-4862-8239-e4debf0f873e" />
</details>

<br>

## 🏗️ 시스템 아키텍처
<img width="598" height="588" alt="스크린샷 2025-10-10 오후 8 52 10" src="https://github.com/user-attachments/assets/ab84ce8a-995b-44f9-832d-65643b8dc444" />


<br>

## 🛠️ 기술 스택

| 구분 | 기술 |
| :--- | :--- |
| **Backend** | `Java 21`, `Spring Boot 3`, `Spring Security`, `JPA`, `QueryDSL`, `JWT`, `JUnit5` |
| **Database** | `MySQL`, `Redis` |
| **Infrastructure** | `AWS (EC2, RDS, S3, VPC)`, `Docker`, `Docker Compose`, `Nginx` |
| **CI/CD** | `GitHub Actions`, `Vercel` | 
| **Monitoring** | `Prometheus`, `Grafana`|
| **Frontend** | `SvelteKit` |

<br>
