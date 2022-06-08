(ns user
  (:require [aero.core :as aero]
            [clojure.java.io :as io]
            [clojure.zip :as zip]
            [integrant.core :as ig]
            [integrant.repl :as ig-repl]))

(defmethod aero/reader 'ig/ref
  [_ _ value]
  (ig/ref value))

(defn load-config
  [profile]
  (aero/read-config "config/dev.edn" {:profile profile}))

(comment
  ;; require async
  (require '[clojure.core.async :refer [go thread >! <! <!! >!! chan put! take!]])
  ;; require other
  (require '[org.httpkit.client :as http]
           '[clojure.xml :as xml]
           '[clojure.zip :as zip]
           '[clojure.java.io :as io]
           '[crouton.html :as crouton]
           '[clojure.data.zip.xml :refer :all])
  ;; prepare integrant
  (ig-repl/set-prep!
    (fn []
      (let [config (load-config :dev)]
        (ig/load-namespaces config [:services.scheduling/weather])
        (ig/prep config [:services.scheduling/weather]))))
  ;; integrant repl
  (ig-repl/prep [:services.scheduling/weather])
  (ig-repl/init [:services.scheduling/weather])
  (ig-repl/go [:services.scheduling/weather])
  (ig-repl/halt)
  (ig-repl/reset)
  (ig-repl/clear)
  (ig-repl/load-namespaces config [:services.scheduling/weather])

  ;; try weather
  (-> (slurp "http://api.weatherapi.com/v1/current.json?key={WEATHER_API_KEY}&q=Kaliningrad&aqi=no&alerts=no")
      (jsonista.core/read-value jsonista.core/keyword-keys-object-mapper)
      :current
      (select-keys [:temp_c :humidity :condition :wind_kph :wind_dir :feels_like]))

  ;; weatherapi.com current response
  (def wresponse
    {:current {:gust_mph 8.3,
               :last_updated_epoch 1654438500,
               :is_day 1,
               :temp_f 64.4,
               :feelslike_f 64.4,
               :pressure_in 30.18,
               :wind_degree 360,
               :cloud 50,
               :precip_mm 0.0,
               :last_updated "2022-06-05 16:15",
               :gust_kph 13.3,
               :temp_c 18.0,
               :humidity 64,
               :pressure_mb 1022.0,
               :vis_miles 6.0,
               :condition {:icon "//cdn.weatherapi.com/weather/64x64/day/116.png", :code 1003, :text "Partly cloudy"},
               :wind_mph 9.4,
               :feelslike_c 18.0,
               :vis_km 10.0,
               :wind_kph 15.1,
               :wind_dir "N",
               :uv 5.0,
               :precip_in 0.0},
     :location {:name "Kaliningrad",
                :localtime "2022-06-05 16:28",
                :region "Kaliningrad",
                :lon 20.5,
                :lat 54.71,
                :tz_id "Europe/Kaliningrad",
                :country "Russia",
                :localtime_epoch 1654439315}})

  )