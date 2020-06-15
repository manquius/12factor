FROM gradle:6.5.0-jre14 AS builder
COPY ./ /consumer
WORKDIR /consumer
RUN gradle build

FROM adoptopenjdk/openjdk14:jdk-14.0.1_7-alpine-slim
COPY --from=builder /consumer/build/libs/consumer-*-all.jar consumer.jar
EXPOSE 8080
CMD ["java", "-Dcom.sun.management.jmxremote", "-Xmx128m", "-jar", "consumer.jar"]
