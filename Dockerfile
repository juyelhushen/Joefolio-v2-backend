FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY .pmd/ .pmd/
COPY .checkstyle/ .checkstyle/
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw && ./mvnw --batch-mode --no-transfer-progress dependency:go-offline

COPY src ./src
RUN ./mvnw --batch-mode --no-transfer-progress -DskipTests package \
    && find target -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' -exec cp {} /workspace/app.jar \;

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /workspace/app.jar /app/app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
