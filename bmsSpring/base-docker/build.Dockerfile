FROM builder as build
WORKDIR /app
ARG MODULE_NAME

COPY --from=builder /root/.m2 /root/.m2
COPY ${MODULE_NAME}/src ./submodule/src
COPY ${MODULE_NAME}/pom.xml    submodule/pom.xml


WORKDIR /app/submodule
RUN mvn -q -e clean package -DskipTests -Dmaven.compiler.showWarnings=true -Dmaven.compiler.source=22 -Dmaven.compiler.target=22 -Dmaven.compiler.compilerArgs=--enable-preview
RUN mv target/*.jar target/app.jar