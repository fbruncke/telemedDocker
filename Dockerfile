

#FROM henrikbaerbak/jdk17-gradle74 as builder
FROM gradle:7.6.1-jdk17-focal AS builder


LABEL maintainer="fha@easv.dk"

#variables for build and run-time
ARG SERVER_PORT_NO=4567
 

WORKDIR /telemedserver

COPY broker/src /telemedserver/telemed/broker/src/
COPY broker/build.gradle /telemedserver/telemed/broker/

COPY telemed/build.gradle     /telemedserver/telemed/telemed/
COPY telemed/gradle.properties     /telemedserver/telemed/telemed/
COPY telemed/src     /telemedserver/telemed/telemed/src/

COPY build.gradle /telemedserver/telemed/
COPY settings.gradle /telemedserver/telemed/

WORKDIR /telemedserver/telemed/

#RUN gradle serverHttp
#RUN gradle --no-daemon

#execute as part of the build step
RUN gradle jar

#---------------------------------------------------
#Create second build container
#FROM openjdk:17-alpine
FROM eclipse-temurin:17-jre-focal

LABEL maintainer="fha@easv.dk"

#Not working ??
ARG DB_TYPE=memory
ENV DB_TYPE=$DB_TYPE  

WORKDIR /home/

#copy from first container build step
COPY --from=builder /telemedserver/telemed/telemed/build/libs/telemed.jar telemed.jar

#Expose port no
EXPOSE $SERVER_PORT_NO

#CMD exec java -jar telemed.jar "memory" false false

#ENTRYPOINT java -jar telemed.jar "memory" false false

#Execute when container is started
CMD exec java -jar telemed.jar "$DB_TYPE" false false

#docker build -t master .

#docker run -ti --rm master bash
#docker run -p 4567:4567 -ti --rm master bash
#docker run -p 4567:4567 -ti --rm master

