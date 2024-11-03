FROM jelastic/maven:3.9.8-openjdk-22.0.2-almalinux-9 AS build

WORKDIR /app

# copy install template module
COPY template-module/pom.xml template-module/pom.xml
COPY template-module/src template-module/src

#build template module
RUN mvn -f template-module/pom.xml dependency:go-offline -B
RUN mvn -f template-module/pom.xml clean install -DskipTests -Dmaven.compiler.showWarnings=true -Dmaven.compiler.source=22 -Dmaven.compiler.target=22 -Dmaven.compiler.compilerArgs=--enable-preview

# copy install day service
COPY day-service/pom.xml day-service/pom.xml
COPY day-service/src day-service/src

#build day service
RUN mvn -f day-service/pom.xml dependency:go-offline -B
RUN mvn -f day-service/pom.xml clean package -DskipTests -Dmaven.compiler.showWarnings=true -Dmaven.compiler.source=22 -Dmaven.compiler.target=22 -Dmaven.compiler.compilerArgs=--enable-preview

FROM openjdk:22-jdk-slim
WORKDIR /app

RUN apt-get update && apt-get install -y curl netcat-openbsd && rm -rf /var/lib/apt/lists/*


COPY --from=build /app/day-service/target/day-service-0.0.1-SNAPSHOT.jar .


ENTRYPOINT ["java", "--enable-preview","-Dreactor.schedulers.defaultBoundedElasticOnVirtualThreads=true", "-jar", "day-service-0.0.1-SNAPSHOT.jar"]
