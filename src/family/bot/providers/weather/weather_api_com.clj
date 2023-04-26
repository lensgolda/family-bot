(ns family.bot.providers.weather.weather-api-com
  (:require [clojure.tools.logging :as log]
            [family.bot.providers.spec :as providers-spec]
            [integrant.core :as ig]
            [jsonista.core :as j])
  (:import (java.text DecimalFormat)
           (java.util Locale)
           (family.bot.providers.weather WeatherFetching)
           (java.time LocalDateTime)
           (java.time.format DateTimeFormatter)))

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
        keys-for-hour (conj keys-common :time :is_day :will_it_rain :will_it_snow :condition)
        keys-for-location [:name :localtime :lat :lon]]
    {:location (some-> (:location raw) (select-keys keys-for-location))
     :current (some-> (:current raw) (select-keys keys-common))
     :by-hour (some->> (:hour dayly) (map #(select-keys % keys-for-hour)))
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

(defn- format-hour-time
  "yyyy-MM-dd HH:mm -> HH:mm formatter"
  [time-str]
  (-> time-str
      (LocalDateTime/parse (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm"))
      (.format (DateTimeFormatter/ofPattern "HH:mm"))))

(defn- format-hour
  [hour-forcast]
  (-> hour-forcast
      (select-keys [:time :temp_c])
      (update :time format-hour-time)
      ((juxt :time :temp_c))))

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

(defn- format-by-hour
  [hour-forecast]
  (->> hour-forecast 
       (map (comp
             (partial apply format "%s \\- %.0f\u2103\n")
             format-hour))
       (drop 8)
       (apply str "*Почасовой прогноз:*\n")))

#_(defn- format-weather
  [weather]
  (some-> weather :current format-current))

(defn- format-weather
  [weather]
  (let [[current hour] (some-> weather
                               (update :current format-current)
                               (update :by-hour format-by-hour)
                               ((juxt :current :by-hour)))]
    (str current "\n\n" hour)))

(defrecord WeatherApiCom []
  WeatherFetching
  (fetch-weather [this]
    (some-> (fetch-weather* this)
            (format-weather))))

(defmethod ig/pre-init-spec ::weather
  [_]
  ::providers-spec/weather-api-provider-config)

(defmethod ig/init-key ::weather
  [_ config]
  (map->WeatherApiCom config))
