# Base image for Maven builds
FROM jelastic/maven:3.9.9-openjdk-22.0.2-almalinux-9 AS maven-base
WORKDIR /app

# ---------------------- TEMPLATE MODULE ----------------------
FROM maven-base AS template-module
COPY template-module/pom.xml template-module/pom.xml
RUN mvn -f template-module/pom.xml dependency:go-offline -B
COPY template-module/src template-module/src
RUN mvn -f template-module/pom.xml clean install -DskipTests -Dmaven.compiler.showWarnings=true -Dmaven.compiler.source=22 -Dmaven.compiler.target=22 -Dmaven.compiler.compilerArgs=--enable-preview

# ---------------------- CART SERVICE  ----------------------
FROM maven-base AS cart-service
COPY --from=template-module /root/.m2 /root/.m2
COPY --from=redis-cache /root/.m2 /root/.m2
COPY cart-service/pom.xml cart-service/pom.xml
RUN mvn -f cart-service/pom.xml dependency:go-offline -B
COPY cart-service/src cart-service/src
RUN mvn -f cart-service/pom.xml clean package -DskipTests -Dmaven.compiler.showWarnings=true -Dmaven.compiler.source=22 -Dmaven.compiler.target=22 -Dmaven.compiler.compilerArgs=--enable-preview

# ---------------------- RUNTIME IMAGE ----------------------
FROM alpine/java:22.0.2-jre AS runtime
WORKDIR /app

RUN apk add --no-cache curl netcat-openbsd


COPY --from=cart-service /app/cart-service/target/cart-service-0.0.1-SNAPSHOT.jar .


ENTRYPOINT ["java", "--enable-preview", "-Dreactor.schedulers.defaultBoundedElasticOnVirtualThreads=true","-jar", "cart-service-0.0.1-SNAPSHOT.jar"]
