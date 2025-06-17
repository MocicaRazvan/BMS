FROM jelastic/maven:3.9.9-openjdk-22.0.2-almalinux-9 AS ollama-search-deps
COPY ../pom.xml .
COPY ../ollama-search/pom.xml ollama-search/pom.xml
RUN mvn -f ollama-search/pom.xml dependency:go-offline -B -q -e

FROM ollama-search-deps AS ollama-search-build
COPY --from=ollama-search-deps /root/.m2 /root/.m2
COPY ../ollama-search/src ollama-search/src
RUN mvn -f ollama-search/pom.xml clean install -DskipTests -Dmaven.compiler.showWarnings=true -Dmaven.compiler.source=22 -Dmaven.compiler.target=22 -Dmaven.compiler.compilerArgs=--enable-preview \
    -q -e
