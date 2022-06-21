FROM clojure:temurin-17-tools-deps-alpine

RUN apk add --no-cache rlwrap

RUN mkdir -p /opt/family-bot
COPY . /opt/family-bot
WORKDIR /opt/family-bot

# RUN clj -T:build uber
CMD clj -M -m family.bot.core prod
