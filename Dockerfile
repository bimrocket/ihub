FROM openjdk:11.0.15-jre

ENV TZ Europe/Madrid
ENV JAR_FILE ihub-1.1-full.jar

RUN apt-get update
RUN apt-get install curl -y 
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /opt

ADD ihub-server/target/${JAR_FILE} /opt/
ADD ihub-server/src/main/resources/application.properties /opt/



COPY entrypoint.sh /usr/local/bin
RUN chmod +x /usr/local/bin/entrypoint.sh

RUN mkdir logs

EXPOSE 8080
ENTRYPOINT ["entrypoint.sh"]
