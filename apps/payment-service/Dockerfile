FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn --batch-mode --no-transfer-progress dependency:go-offline

COPY src ./src
RUN mvn --batch-mode --no-transfer-progress clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy

RUN apt-get update \
    && apt-get install --yes --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system minishop \
    && useradd --system --gid minishop --home-dir /app minishop

WORKDIR /app

COPY --from=build --chown=minishop:minishop /workspace/target/payment-service-*.jar app.jar

USER minishop

EXPOSE 8086

HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=5 \
  CMD curl --fail --silent http://localhost:8086/actuator/health > /dev/null || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]

