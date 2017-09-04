FROM openjdk:8-jdk-alpine
VOLUME /tmp
ADD target/absolute-0.0.1.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=dev -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
