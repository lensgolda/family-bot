(ns family.bot.providers.rates)

(defprotocol RatesFetching
  (fetch-rates [this]))

(defn fetch-rates!
  [<provider>]
  (fetch-rates <provider>))