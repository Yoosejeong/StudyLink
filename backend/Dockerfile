# 베이스 이미지 지정
# as builder: 별칭 정해서 나중에 이 빌드 과정을 참조할 수 있게함 => 멀티 스테이지 빌드
FROM eclipse-temurin:21-jdk-alpine AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle Wrapper 복사 => 호스트 머신에 Maven 설치 없이 프로젝트의 Gradle Wrapper 를 사용하여 빌드
# src보다 먼저 복사하여 의존성 변경 시에만 이 레이어가 재빌드되도록 설정 => 레이어 캐싱 활용
COPY gradlew ./
COPY gradle ./gradle
RUN chmod +x ./gradlew

# 의존성 파일 복사 + 다운로드
COPY build.gradle settings.gradle ./
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src ./src

# 프로젝트 빌드 + 패키징 => JAR 파일 생성
# daemon: Gradle에서 빌드 속도를 높히기 위해 사용하는 백그라운드 프로세스 (사용이 기본값)
# => CI/Docker 빌드는 대부분 단발성이기에 프로세스를 끔
# -x test: 테스트코드 검증 없애기
RUN #./gradlew build --no-daemon
RUN ./gradlew build -x test --no-daemon


# 실행 => 빌드된 애플리케이션 실행을 위한 환경 구성
# JRE만 포함된 경량 이미지를 사용하여 최종 이미지 크기를 최소화
FROM eclipse-temurin:21-jre-alpine

# 작업 디렉토리 설정
WORKDIR /app

# 빌드 결과물 복사
# --from=builder: 위에서 as builder 한 것을 참조
COPY --from=builder /app/build/libs/*.jar app.jar

# 기본 포트
EXPOSE 8080

# 컨테이너 시작 시 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]