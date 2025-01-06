FROM jelastic/maven:3.9.9-openjdk-22.0.2-almalinux-9 AS build

WORKDIR /app

# copy install template module
COPY template-module/pom.xml template-module/pom.xml
COPY template-module/src template-module/src

#build template module
RUN mvn -f template-module/pom.xml dependency:go-offline -B
RUN mvn -f template-module/pom.xml clean install -DskipTests -Dmaven.compiler.showWarnings=true -Dmaven.compiler.source=22 -Dmaven.compiler.target=22 -Dmaven.compiler.compilerArgs=--enable-preview

# copy install cache module
COPY redis-cache/pom.xml redis-cache/pom.xml
COPY redis-cache/src redis-cache/src

#build cache module
RUN mvn -f redis-cache/pom.xml dependency:go-offline -B
RUN mvn -f redis-cache/pom.xml clean install -DskipTests -Dmaven.compiler.showWarnings=true -Dmaven.compiler.source=22 -Dmaven.compiler.target=22 -Dmaven.compiler.compilerArgs=--enable-preview

# copy install ollama module
COPY ollama-search/pom.xml ollama-search/pom.xml
COPY ollama-search/src ollama-search/src
#build ollama module
RUN mvn -f ollama-search/pom.xml dependency:go-offline -B
RUN mvn -f ollama-search/pom.xml clean install -DskipTests -Dmaven.compiler.showWarnings=true -Dmaven.compiler.source=22 -Dmaven.compiler.target=22 -Dmaven.compiler.compilerArgs=--enable-preview

# copy install user service
COPY user-service/pom.xml user-service/pom.xml
COPY user-service/src user-service/src

#build user service
RUN mvn -f user-service/pom.xml dependency:go-offline -B
RUN mvn -f user-service/pom.xml clean package -DskipTests -Dmaven.compiler.showWarnings=true -Dmaven.compiler.source=22 -Dmaven.compiler.target=22 -Dmaven.compiler.compilerArgs=--enable-preview

FROM alpine/java:22.0.2-jre AS runtime
WORKDIR /app

RUN apk add --no-cache curl netcat-openbsd


COPY --from=build /app/user-service/target/user-service-0.0.1-SNAPSHOT.jar .


ENTRYPOINT ["java", "--enable-preview","-Dreactor.schedulers.defaultBoundedElasticOnVirtualThreads=true", "-jar", "user-service-0.0.1-SNAPSHOT.jar"]
