FROM openjdk:8-jre
COPY build/libs/heartnal-0.0.1-SNAPSHOT.jar service.jar
ENTRYPOINT ["java", "-jar", "service.jar"]
ENV TZ Asia/Seoul
