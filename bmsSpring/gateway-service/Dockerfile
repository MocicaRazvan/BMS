FROM jelastic/maven:3.9.8-openjdk-22.0.2-almalinux-9 AS build
WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn package -DskipTests -Dmaven.compiler.source=22 -Dmaven.compiler.target=22 -Dmaven.compiler.compilerArgs=--enable-preview


FROM openjdk:22-jdk-slim
WORKDIR /app
COPY --from=build /app/target/gateway-service-0.0.1-SNAPSHOT.jar .

ENTRYPOINT ["java", "--enable-preview", "-jar", "gateway-service-0.0.1-SNAPSHOT.jar"]
