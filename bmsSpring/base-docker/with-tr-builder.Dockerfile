# syntax = devthefuture/dockerfile-x

FROM jelastic/maven:3.9.9-openjdk-22.0.2-almalinux-9 AS builder
ARG MODULE_NAME
COPY --from=./template-module.Dockerfile#template-module-build /root/.m2 /root/.m2
COPY --from=./redis-module.Dockerfile#redis-cache-build /root/.m2 /root/.m2
COPY --from=./parent-module.Dockerfile /root/.m2 /root/.m2
COPY pom.xml .
COPY ${MODULE_NAME}/pom.xml ${MODULE_NAME}/pom.xml
RUN mvn -f ${MODULE_NAME}/pom.xml dependency:go-offline -B -q -e

