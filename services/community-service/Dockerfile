# community-service 이미지 (멀티 스테이지: 빌드 → 실행)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon > /dev/null 2>&1 || true
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/community-service.jar app.jar
EXPOSE 8084
# GEMINI_API_KEY 등 비밀값은 실행 시 환경 변수(K8s Secret)로 주입
ENTRYPOINT ["java", "-jar", "app.jar"]
