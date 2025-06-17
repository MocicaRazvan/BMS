FROM jelastic/maven:3.9.9-openjdk-22.0.2-almalinux-9
COPY ../pom.xml .
RUN mvn install -N -B -q -e
