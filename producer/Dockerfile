FROM gradle:6.5.0-jre14 as cache
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME /home/gradle/cache_home
COPY build.gradle /home/gradle/java-code/
COPY gradle.properties /home/gradle/java-code/
WORKDIR /home/gradle/java-code
RUN gradle clean build -i --stacktrace

FROM gradle:6.5.0-jre14 AS builder
COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
COPY ./ /producer
WORKDIR /producer
RUN gradle build

FROM adoptopenjdk/openjdk14:jdk-14.0.1_7-alpine-slim
COPY --from=builder /producer/build/libs/producer-*-all.jar producer.jar
EXPOSE 8080
CMD ["java", "-Dcom.sun.management.jmxremote", "-Xmx128m", "-jar", "producer.jar"]
