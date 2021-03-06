(ns family.bot.providers.weather.weather-api-com
  (:require [clojure.tools.logging :as log]
            [family.bot.providers.spec :as providers-spec]
            [integrant.core :as ig]
            [jsonista.core :as j])
  (:import (java.text DecimalFormat)
           (java.util Locale)))

(def ^:private i18map->format
  {:temp_c "температура: %s\u2103\n"
   :wind_kph "ветер %s км/ч"
   :wind_dir ", направление: %s\n"
   :humidity "влажность %s%\u0025\n"
   :feelslike_c "ощущается как %s\u2103\n"
   :precip_mm "осадки %s мм\n"
   :cloud "облачность %s%\u0025\n"})

(defn- raw->tidy
  [raw]
  (let [forecast (:forecast raw)
        dayly (get-in forecast [:forecastday 0])
        keys-common [:temp_c :wind_kph :wind_dir :humidity :feelslike_c :precip_mm :cloud]
        keys-for-day [:mintemp_c :maxtemp_c :condition :daily_chance_of_rain]
        keys-for-hour (conj keys-common :time :is_day)
        keys-for-location [:name :localtime :lat :lon]
        hour-by-hour (some->> (:hour dayly)
                              (map #(select-keys % keys-for-hour)))]
    {:location (some-> (:location raw) (select-keys keys-for-location))
     :current (some-> (:current raw) (select-keys keys-common))
     :by-hour hour-by-hour
     :day (some-> (:day dayly) (select-keys keys-for-day))}))

(defn- fetch-weather*
  [{:keys [base-url key]}]
  (try
    (let [url (format "%s&key=%s" base-url key)
          resp (some-> (slurp url)
                       (j/read-value j/keyword-keys-object-mapper))]
      (log/info "fetch-weather response: " resp)
      (raw->tidy resp))
    (catch Exception e
      (log/error e "error fetching weather"))))

(defn- number->decimal-str
  [^Number number]
  (let [df (DecimalFormat/getInstance (Locale/forLanguageTag "ru-RU"))]
    (.setMaximumFractionDigits df 2)
    (.format df number)))

(defn- current-map->strings
  [current]
  (for [[k v] current
        :let [fkey (get i18map->format k "%s")
              fval (if (number? v)
                     (number->decimal-str v)
                     (str v))]
        :when (some? v)]
    (format fkey fval)))

(defn- format-current
  [current]
  (as-> current $
    (current-map->strings $)
    (apply str "*Погода сейчас*:\n" $)))

(defn- format-weather
  [weather]
  (some-> weather :current format-current))

(defrecord WeatherApiCom []
  family.bot.providers.weather/WeatherFetching
  (fetch-weather [this]
    (some-> (fetch-weather* this)
            (format-weather))))

(defmethod ig/pre-init-spec :family.bot.providers.weather/weather-api-com
  [_]
  ::providers-spec/weather-api-provider-config)

(defmethod ig/init-key :family.bot.providers.weather/weather-api-com
  [_ config]
  (map->WeatherApiCom config))
