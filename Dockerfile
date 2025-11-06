FROM openjdk:26-ea-trixie
VOLUME /tmp
COPY target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app.jar"]