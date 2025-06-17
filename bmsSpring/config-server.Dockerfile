# syntax = devthefuture/dockerfile-x
ARG MODULE_NAME=config-server

INCLUDE ./base-docker/independent-deps.Dockerfile

INCLUDE ./base-docker/independent-build.Dockerfile

INCLUDE ./base-docker/runtime.Dockerfile

ENTRYPOINT ["java", "--enable-preview", "-Dreactor.schedulers.defaultBoundedElasticOnVirtualThreads=true","-jar", "app.jar"]
