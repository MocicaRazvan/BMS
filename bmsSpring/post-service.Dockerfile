# syntax = devthefuture/dockerfile-x

ARG MODULE_NAME=post-service


INCLUDE ./base-docker/with-tro-builder.Dockerfile

INCLUDE ./base-docker/build.Dockerfile

# ---------------------- RUNTIME IMAGE ----------------------
INCLUDE ./base-docker/runtime.Dockerfile

ENTRYPOINT ["java","--enable-preview","-Dreactor.schedulers.defaultBoundedElasticOnVirtualThreads=true","-jar", "app.jar"]