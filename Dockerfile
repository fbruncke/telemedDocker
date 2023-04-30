
#First build step, create the Java jar executeable for the final docker image 
FROM gradle:7.6.1-jdk17-focal AS builder

#Set the author details
LABEL maintainer="fha@easv.dk"

WORKDIR /telemedserver

#Copy needed resources to build Jar file
COPY broker/src broker/src
COPY broker/build.gradle broker/build.gradle
COPY telemed/src telemed/src
COPY telemed/build.gradle telemed/build.gradle
COPY telemed/gradle.properties telemed/gradle.properties
COPY build.gradle build.gradle
COPY settings.gradle settings.gradle

#Execute gradle and build the jar file as part of the build step
RUN gradle jar

#Second build step, builds the docker image, used for distribution 
FROM eclipse-temurin:17-jre-focal

#variables for build and run-time
ARG DB_URI=memory
ENV DB_URI=$DB_URI  

WORKDIR /root

#copy from first container build step
COPY --from=builder /telemedserver/telemed/build/libs/telemed.jar telemed.jar

#Expose port no
EXPOSE 4567

#Execute when container is started (Arguments: DB host,TLS,performance hack)
CMD exec java -jar telemed.jar "$DB_URI" false false
