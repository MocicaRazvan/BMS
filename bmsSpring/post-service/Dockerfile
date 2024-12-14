FROM jelastic/maven:3.9.8-openjdk-22.0.2-almalinux-9 AS build

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

# copy install cache module
COPY ollama-search/pom.xml ollama-search/pom.xml
COPY ollama-search/src ollama-search/src
#build ollama module
RUN mvn -f ollama-search/pom.xml dependency:go-offline -B
RUN mvn -f ollama-search/pom.xml clean install -DskipTests -Dmaven.compiler.showWarnings=true -Dmaven.compiler.source=22 -Dmaven.compiler.target=22 -Dmaven.compiler.compilerArgs=--enable-preview

# copy install post service
COPY post-service/pom.xml post-service/pom.xml
COPY post-service/src post-service/src

#build post service
RUN mvn -f post-service/pom.xml dependency:go-offline -B
RUN mvn -f post-service/pom.xml clean package -DskipTests -Dmaven.compiler.showWarnings=true -Dmaven.compiler.source=22 -Dmaven.compiler.target=22 -Dmaven.compiler.compilerArgs=--enable-preview

FROM openjdk:22-jdk-slim
WORKDIR /app

RUN apt-get update && apt-get install -y curl netcat-openbsd && rm -rf /var/lib/apt/lists/*


COPY --from=build /app/post-service/target/post-service-0.0.1-SNAPSHOT.jar .


ENTRYPOINT ["java", "--enable-preview","-Dreactor.schedulers.defaultBoundedElasticOnVirtualThreads=true", "-jar", "post-service-0.0.1-SNAPSHOT.jar"]
