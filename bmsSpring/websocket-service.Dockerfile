# syntax = devthefuture/dockerfile-x
ARG MODULE_NAME=websocket-service

INCLUDE ./base-docker/independent-deps.Dockerfile

INCLUDE ./base-docker/independent-build.Dockerfile

INCLUDE ./base-docker/runtime.Dockerfile

ENTRYPOINT ["java", "--enable-preview", "-jar", "app.jar"]
