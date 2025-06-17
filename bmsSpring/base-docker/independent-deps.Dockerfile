FROM jelastic/maven:3.9.9-openjdk-22.0.2-almalinux-9 AS deps

ARG MODULE_NAME

WORKDIR /app

COPY ../pom.xml .

COPY ${MODULE_NAME}/pom.xml submodule/pom.xml

WORKDIR /app/submodule

RUN mvn dependency:go-offline \
      -B -q -e
