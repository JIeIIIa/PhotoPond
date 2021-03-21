#!/bin/sh
set -e

exec java -XX:+PrintFlagsFinal -XX:MaxRAMPercentage=85.0 -version | grep -Ei "maxheapsize|maxram"

exec java -XX:MaxRAMPercentage=85.0 \
          -XX:OnOutOfMemoryError="sleep 5 && kill %p &" \
          -Dspring.profiles.active=$ACTIVE_PROFILES \
          -DJDBC_DATABASE_URL=$JDBC_DATABASE_URL \
          -DJDBC_DATABASE_USERNAME=$JDBC_DATABASE_USERNAME \
          -DJDBC_DATABASE_PASSWORD=$JDBC_DATABASE_PASSWORD \
          -DFACEBOOK_APPLICATION_ID=$FACEBOOK_APPLICATION_ID \
          -DFACEBOOK_APPLICATION_SECRET=$FACEBOOK_APPLICATION_SECRET \
          -DTWITTER_CONSUMER_KEY=$TWITTER_CONSUMER_KEY \
          -DTWITTER_CONSUMER_SECRET=$TWITTER_CONSUMER_SECRET \
          -DKEY_STORE_PASSWORD=$KEY_STORE_PASSWORD \
          -jar target/photopond.jar
