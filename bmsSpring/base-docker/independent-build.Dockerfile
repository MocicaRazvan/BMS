FROM  deps AS build

WORKDIR /app

COPY --from=deps /root/.m2 /root/.m2

COPY ${MODULE_NAME}/src ./submodule/src

WORKDIR /app/submodule

RUN mvn -q -e package -DskipTests -Dmaven.compiler.source=22 -Dmaven.compiler.target=22 -Dmaven.compiler.compilerArgs=--enable-preview

RUN mv target/*.jar target/app.jar