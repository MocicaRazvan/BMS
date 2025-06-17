FROM jelastic/maven:3.9.9-openjdk-22.0.2-almalinux-9 AS redis-cache-deps
COPY ../pom.xml .
COPY ../redis-cache/pom.xml redis-cache/pom.xml
RUN mvn -f redis-cache/pom.xml dependency:go-offline -B -q -e

FROM redis-cache-deps AS redis-cache-build
COPY --from=redis-cache-deps /root/.m2 /root/.m2
COPY ../redis-cache/src redis-cache/src
RUN mvn -f redis-cache/pom.xml clean install -DskipTests -Dmaven.compiler.showWarnings=true -Dmaven.compiler.source=22 -Dmaven.compiler.target=22 -Dmaven.compiler.compilerArgs=--enable-preview \
    -q -e
