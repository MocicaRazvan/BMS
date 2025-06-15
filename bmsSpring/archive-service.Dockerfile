# Base image for Maven build
FROM jelastic/maven:3.9.9-openjdk-22.0.2-almalinux-9 AS build
WORKDIR /app

COPY /archive-service/pom.xml .

RUN mvn dependency:go-offline -B -q

COPY /archive-service/src ./src

RUN mvn -q package -DskipTests -Dmaven.compiler.source=22 -Dmaven.compiler.target=22 -Dmaven.compiler.compilerArgs=--enable-preview


# ---------------------- RUNTIME IMAGE ----------------------
FROM alpine/java:22.0.2-jre AS runtime
WORKDIR /app

RUN addgroup -g 1000 -S appgroup \
 && adduser  -u 1000 -S -G appgroup \
      -h /home/appuser \
      -s /sbin/nologin \
      -D appuser

ENV HOME=/home/appuser

RUN mkdir -p archive/data \
 && chown 1000:1000 archive/data \
 && chmod 770 archive/data \
 && chmod g+s archive/data

VOLUME ["/app/archive/data"]

COPY --from=build /app/target/archive-service-0.0.1-SNAPSHOT.jar .

RUN chown -R appuser:appgroup /app

USER appuser:appgroup

ENTRYPOINT ["java","--enable-preview","-Dreactor.schedulers.defaultBoundedElasticOnVirtualThreads=true","-jar", "archive-service-0.0.1-SNAPSHOT.jar"]