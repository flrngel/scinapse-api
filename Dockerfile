FROM openjdk:8-jdk-alpine
ADD run.sh /run.sh
RUN chmod 700 /run.sh
ADD build/resources/main/git.properties /git.properties
ADD build/libs/scinapse-api.jar /app.jar
EXPOSE 8080
CMD [ "/bin/ash", "-c", "/run.sh"]