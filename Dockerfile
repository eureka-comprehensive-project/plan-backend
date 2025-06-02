# 1. Java 17 환경 기반 이미지 사용
FROM openjdk:17-jdk-alpine

# 2. 빌드된 jar 파일 복사
COPY build/libs/*.jar app.jar

# 3. 컨테이너 실행 시 실행할 명령
ENTRYPOINT ["java", "-jar", "/app.jar"]