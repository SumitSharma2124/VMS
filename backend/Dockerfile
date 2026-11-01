FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace/vms
COPY vms/gradlew vms/settings.gradle vms/build.gradle ./
COPY vms/gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon
COPY vms/src ./src
RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:25-jre
RUN useradd --system --create-home --uid 10001 vms
WORKDIR /app
COPY --from=build /workspace/vms/build/libs/*.jar /app/app.jar
RUN chown -R vms:vms /app
USER vms
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
