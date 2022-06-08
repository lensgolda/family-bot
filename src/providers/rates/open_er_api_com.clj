(ns providers.rates.open-er-api-com
  (:require [clojure.core.async :refer [go thread >! <! <!! >!! chan put! take!]]
            [integrant.core :as ig]
            [jsonista.core :as j]
            [services.messaging.telegram :as telegram]
            [providers.spec :as providers-spec]))


(defn- fetch-rate
  [{url :base-url} currency]
  (thread
    (let [kw (keyword currency)
          uri (format "%s/%s" url currency)
          res (-> (slurp uri)
                  (j/read-value j/keyword-keys-object-mapper)
                  :rates)]
      (into [kw] [[:RUB (:RUB res)]]))))

(defn- fetch-rates*
  [{:keys [currencies] :as <provider>}]
  (when (seq currencies)
    (for [c currencies
          :let [ch (fetch-rate <provider> c)]]
      (<!! ch))))

(defn- rates-vec->strings
  [rates]
  (for [r rates
        :let [[k1 [k2 v]] r]]
    (format "\uD83D\uDCB1 %s\t%.2f\t%s\n" (telegram/emoji k1) v (telegram/emoji k2))))

(defn- format-rates
  [rates]
  (->> (rates-vec->strings rates)
       (apply str "*Курсы валют*\n")))

(defrecord OpenErApiCom []
  providers.rates/RatesFetching
  (fetch-rates [this]
    (some->> (fetch-rates* this)
             (format-rates))))

(defmethod ig/pre-init-spec :providers.rates/open-er-api-com
  []
  ::providers-spec/rates-provider)

(defmethod ig/init-key :providers.rates/open-er-api-com
  [_ config]
  (map->OpenErApiCom config))