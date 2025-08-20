# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Gradle wrapper & 설정 우선 복사 (캐시 최적화)
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew

# 소스 복사 후 빌드
COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# (선택) 로캘/타임존
ENV TZ=Asia/Seoul

# 빌드 산출물 복사
COPY --from=build /workspace/build/libs/*.jar /app/app.jar

# Render가 넘겨주는 $PORT로 리슨 + prod 프로필 활성화
CMD ["sh", "-c", "java -XX:MaxRAMPercentage=75.0 -Dserver.port=${PORT:-8080} -Dspring.profiles.active=prod -jar /app/app.jar"]
