FROM maven:3.9.12-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY source/agent-platform ./source/agent-platform

WORKDIR /workspace/source/agent-platform
RUN mvn -q -DskipTests -pl agent-boot/boot-app -am package

FROM eclipse-temurin:21-jre

ENV SPRING_PROFILES_ACTIVE=docker \
    JAVA_OPTS=""

WORKDIR /app
RUN useradd --system --uid 10001 --gid 0 agent \
    && mkdir -p /app/logs /app/workspaces /app/storage \
    && chown -R agent:0 /app

COPY --from=build /workspace/source/agent-platform/agent-boot/boot-app/target/boot-app-*.jar /app/app.jar

EXPOSE 3060
USER agent

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
