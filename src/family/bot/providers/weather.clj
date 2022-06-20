(ns family.bot.providers.weather)

(defprotocol WeatherFetching
  (fetch-weather [this]))

(defn fetch-weather!
  [<provider>]
  (fetch-weather <provider>))