# Base image for Maven build
FROM jelastic/maven:3.9.9-openjdk-22.0.2-almalinux-9 AS deps

ARG MODULE_NAME

WORKDIR /app

COPY ../pom.xml .

COPY ${MODULE_NAME}/pom.xml submodule/pom.xml

WORKDIR /app/submodule

RUN mvn dependency:go-offline \
      -B -q -e


FROM  deps AS build

WORKDIR /app

COPY --from=deps /root/.m2 /root/.m2

COPY ${MODULE_NAME}/src ./submodule/src

WORKDIR /app/submodule

RUN mvn -q -e package -DskipTests -Dmaven.compiler.source=22 -Dmaven.compiler.target=22 -Dmaven.compiler.compilerArgs=--enable-preview

RUN mv target/*.jar target/app.jar
# ---------------------- RUNTIME IMAGE ----------------------
FROM alpine/java:22.0.2-jre AS runtime
WORKDIR /app

RUN apk add --no-cache \
      curl \
      netcat-openbsd \
 && addgroup -S appgroup \
 && adduser -S -G appgroup \
      -h /home/appuser \
      -s /sbin/nologin \
      -D appuser

ENV HOME=/home/appuser

COPY --from=build --chown=appuser:appgroup \
     /app/submodule/target/app.jar \
     app.jar

USER appuser:appgroup

ENTRYPOINT ["java", "--enable-preview", "-Dreactor.schedulers.defaultBoundedElasticOnVirtualThreads=true","-jar", "app.jar"]
