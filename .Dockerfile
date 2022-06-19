FROM openjdk:11-jre

COPY ./target/family-bot-0.0.4-standalone.jar /opt/lens/family-bot.jar

EXPOSE 8080

CMD java ${JAVA_OPTS} \
    -Dclojure.tools.logging.factory=clojure.tools.logging.impl/log4j2-factory \
    -Dlog4j.configurationFile=resources/log4j2.properties \
    -Dapp.config=${APP_CONFIG} \
    -jar ./opt/lens/family-bot.jar
