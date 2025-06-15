# Base image for Maven builds
FROM jelastic/maven:3.9.9-openjdk-22.0.2-almalinux-9 AS maven-base
WORKDIR /app

# ---------------------- TEMPLATE MODULE ----------------------
FROM maven-base AS template-module
COPY template-module/pom.xml template-module/pom.xml
RUN mvn -f template-module/pom.xml dependency:go-offline -B
COPY template-module/src template-module/src
RUN mvn -f template-module/pom.xml clean install -DskipTests -Dmaven.compiler.showWarnings=true -Dmaven.compiler.source=22 -Dmaven.compiler.target=22 -Dmaven.compiler.compilerArgs=--enable-preview

# ---------------------- REDIS CACHE MODULE ----------------------
FROM maven-base AS redis-cache
COPY redis-cache/pom.xml redis-cache/pom.xml
RUN mvn -f redis-cache/pom.xml dependency:go-offline -B
COPY redis-cache/src redis-cache/src
RUN mvn -f redis-cache/pom.xml clean install -DskipTests -Dmaven.compiler.showWarnings=true -Dmaven.compiler.source=22 -Dmaven.compiler.target=22 -Dmaven.compiler.compilerArgs=--enable-preview

# ---------------------- OLLAMA SEARCH MODULE ----------------------
FROM maven-base AS ollama-search
COPY ollama-search/pom.xml ollama-search/pom.xml
RUN mvn -f ollama-search/pom.xml dependency:go-offline -B
COPY ollama-search/src ollama-search/src
RUN mvn -f ollama-search/pom.xml clean install -DskipTests -Dmaven.compiler.showWarnings=true -Dmaven.compiler.source=22 -Dmaven.compiler.target=22 -Dmaven.compiler.compilerArgs=--enable-preview

# ---------------------- POST SERVICE  ----------------------
FROM maven-base AS post-service
COPY --from=template-module /root/.m2 /root/.m2
COPY --from=redis-cache /root/.m2 /root/.m2
COPY --from=ollama-search /root/.m2 /root/.m2
COPY post-service/pom.xml post-service/pom.xml
RUN mvn -f post-service/pom.xml dependency:go-offline -B
COPY post-service/src post-service/src
RUN mvn -f post-service/pom.xml clean package -DskipTests -Dmaven.compiler.showWarnings=true -Dmaven.compiler.source=22 -Dmaven.compiler.target=22 -Dmaven.compiler.compilerArgs=--enable-preview

# ---------------------- RUNTIME IMAGE ----------------------
FROM alpine/java:22.0.2-jre AS runtime
WORKDIR /app

RUN addgroup -S appgroup \
 && adduser -S -G appgroup \
      -h /home/appuser \
      -s /sbin/nologin \
      -D appuser

ENV HOME=/home/appuser

COPY --from=post-service /app/post-service/target/post-service-0.0.1-SNAPSHOT.jar .

RUN chown -R appuser:appgroup /app

USER appuser:appgroup

ENTRYPOINT ["java", "--enable-preview","-Dreactor.schedulers.defaultBoundedElasticOnVirtualThreads=true", "-jar", "post-service-0.0.1-SNAPSHOT.jar"]
