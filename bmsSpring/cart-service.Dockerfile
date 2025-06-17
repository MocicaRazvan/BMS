# syntax = devthefuture/dockerfile-x

ARG MODULE_NAME=cart-service

INCLUDE ./base-docker/with-t-builder.Dockerfile

INCLUDE ./base-docker/build.Dockerfile

# ---------------------- RUNTIME IMAGE ----------------------
INCLUDE ./base-docker/runtime.Dockerfile

ENTRYPOINT ["java", "--enable-preview", "-Dreactor.schedulers.defaultBoundedElasticOnVirtualThreads=true","-jar", "app.jar"]
