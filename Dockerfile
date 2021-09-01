FROM openjdk:8u201-jre-alpine3.9
ARG JAR_FILE
WORKDIR /springboot
ADD ${JAR_FILE} app.jar
EXPOSE 8080
COPY src/main/resources ./
ENTRYPOINT ["java","-Dfile.encoding=utf-8","-jar","app.jar"]