FROM maven:3.6-jdk-11-slim as builder

WORKDIR /root/build


# copy the project files
COPY ./pom.xml ./pom.xml
COPY ./src/test/java/ua/kiev/prog/photopond/LoadDependencyDockerTest.java \
     ./src/test/java/ua/kiev/prog/photopond/LoadDependencyDockerTest.java


# build all dependencies for offline use
RUN mvn clean && \
    mvn dependency:resolve dependency:resolve-plugins test

COPY . .

RUN mvn clean && \
    mvn package

FROM adoptopenjdk/openjdk11:alpine-jre

RUN apk --no-cache add curl

ENV ACTIVE_PROFILES \
    JDBC_DATABASE_URL \
    JDBC_DATABASE_USERNAME \
    JDBC_DATABASE_PASSWORD \
    FACEBOOK_APPLICATION_ID \
    FACEBOOK_APPLICATION_SECRET \
    TWITTER_CONSUMER_KEY \
    TWITTER_CONSUMER_SECRET \
    KEY_STORE_PASSWORD

WORKDIR /root/app/

COPY --from=builder /root/build/target/photo-pond*.jar ./target/photopond.jar

COPY docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh

EXPOSE 8095

ENTRYPOINT ["sh", "-c", ". /init.sh && /docker-entrypoint.sh"]
