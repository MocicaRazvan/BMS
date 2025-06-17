# syntax = devthefuture/dockerfile-x

ARG MODULE_NAME=comment-service


INCLUDE ./base-docker/with-tr-builder.Dockerfile

INCLUDE ./base-docker/build.Dockerfile

# ---------------------- RUNTIME IMAGE ----------------------
INCLUDE ./base-docker/runtime.Dockerfile

ENTRYPOINT ["java","--enable-preview","-Dreactor.schedulers.defaultBoundedElasticOnVirtualThreads=true","-jar", "app.jar"]