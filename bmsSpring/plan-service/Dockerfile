FROM jelastic/maven:3.9.8-openjdk-22.0.2-almalinux-9 AS build

WORKDIR /app

# copy install template module
COPY template-module/pom.xml template-module/pom.xml
COPY template-module/src template-module/src

#build template module
RUN mvn -f template-module/pom.xml dependency:go-offline -B
RUN mvn -f template-module/pom.xml clean install -DskipTests -Dmaven.compiler.showWarnings=true -Dmaven.compiler.source=22 -Dmaven.compiler.target=22 -Dmaven.compiler.compilerArgs=--enable-preview

# copy install plan service
COPY plan-service/pom.xml plan-service/pom.xml
COPY plan-service/src plan-service/src

#build plan service
RUN mvn -f plan-service/pom.xml dependency:go-offline -B
RUN mvn -f plan-service/pom.xml clean package -DskipTests -Dmaven.compiler.showWarnings=true -Dmaven.compiler.source=22 -Dmaven.compiler.target=22 -Dmaven.compiler.compilerArgs=--enable-preview

FROM openjdk:22-jdk-slim
WORKDIR /app
COPY --from=build /app/plan-service/target/plan-service-0.0.1-SNAPSHOT.jar .


ENTRYPOINT ["java", "--enable-preview", "-jar", "plan-service-0.0.1-SNAPSHOT.jar"]
