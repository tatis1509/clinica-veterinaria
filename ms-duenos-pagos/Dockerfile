# ---- Etapa 1: compilar con Maven (las pruebas se corren en local / CI con: mvn clean verify) ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

# ---- Etapa 2: imagen liviana solo con el JRE ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/ms-duenos-pagos.jar app.jar
# Render inyecta PORT. MaxRAMPercentage para que quepa en los 512 MB del plan gratis.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -Xss512k"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
