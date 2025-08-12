# 멀티스테이지 빌드
FROM gradle:8.5-jdk21 AS build

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 파일들 복사 (캐시 최적화)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 의존성 다운로드
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src src

# JAR 파일 빌드
RUN ./gradlew build -x test --no-daemon

# 실행 스테이지
FROM openjdk:21-jdk-slim

# 메타데이터
LABEL maintainer="BlockCloud Team"
LABEL version="1.0"
LABEL description="BlockCloud Backend Application"

# 작업 디렉토리 설정
WORKDIR /app

# JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
