(ns providers.weather.weather-api-com
  (:require [integrant.core :as ig]
            [jsonista.core :as j]
            [providers.spec :as providers-spec]))

(defn- fetch-weather*
  [{:keys [base-url key]}]
  (let [url (format "%s&key=%s" base-url key)
        resp (-> (slurp url)
                 (j/read-value j/keyword-keys-object-mapper))]
    (clojure.pprint/pprint resp)
    resp))

(defrecord WeatherApiCom []
  providers.weather/WeatherFetching
  (fetch-weather [this]
    (fetch-weather* this)))

(defmethod ig/pre-init-spec :providers.weather/weather-api-com
  [_]
  ::providers-spec/weather-api-provider-config)

(defmethod ig/init-key :providers.weather/weather-api-com
  [_ config]
  (map->WeatherApiCom config))