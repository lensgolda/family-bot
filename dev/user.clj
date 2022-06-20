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
           '[clojure.data.zip.xml :refer :all])

  ;; set refresh dirs
  (require '[clojure.tools.namespace.repl :refer [set-refresh-dirs]])
  (set-refresh-dirs "src")

  ;; prepare integrant
  (ig-repl/set-prep!
    (fn []
      (let [config (load-config :dev)]
        (ig/load-namespaces config [:family.bot.services.scheduling/weather])
        (ig/prep config [:family.bot.services.scheduling/weather]))))
  ;; integrant repl
  (ig-repl/prep [:family.bot.services.scheduling/weather])
  (ig-repl/init [:family.bot.services.scheduling/weather])
  (ig-repl/go [:family.bot.services.scheduling/weather])
  (ig-repl/halt)
  (ig-repl/reset)
  (ig-repl/clear)

  ;; try weather
  (-> (slurp "http://api.weatherapi.com/v1/forecast.json?key={WEATHER_API_KEY}&q=Kaliningrad&aqi=no&alerts=no")
      (jsonista.core/read-value jsonista.core/keyword-keys-object-mapper))

  )