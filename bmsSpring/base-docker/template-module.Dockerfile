FROM jelastic/maven:3.9.9-openjdk-22.0.2-almalinux-9 AS template-module-deps
COPY ../pom.xml .
COPY ../template-module/pom.xml template-module/pom.xml
RUN mvn -f template-module/pom.xml dependency:go-offline -B -q -e

FROM template-module-deps AS template-module-build
COPY --from=template-module-deps /root/.m2 /root/.m2
COPY ../template-module/src template-module/src
RUN mvn -f template-module/pom.xml clean install -DskipTests -Dmaven.compiler.showWarnings=true -Dmaven.compiler.source=22 -Dmaven.compiler.target=22 -Dmaven.compiler.compilerArgs=--enable-preview \
    -q -e
