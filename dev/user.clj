(ns user
  (:require [aero.core :as aero]
            [clojure.java.io :as io]
            [clojure.zip :as zip]
            [integrant.core :as ig]
            [integrant.repl :as ig-repl]
            [integrant.repl.state :as ig-state]))

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
  (require '[jsonista.core :as json])
  (import '[java.time LocalDateTime LocalDate]
          '[java.time.format DateTimeFormatter])

  ;; set refresh dirs
  (require '[clojure.tools.namespace.repl :refer [set-refresh-dirs]])
  (set-refresh-dirs "src")

  ;; prepare integrant
  (ig-repl/set-prep!
   (fn []
     (let [config (-> (load-config :dev)
                      (select-keys [:family.bot.services.messaging.telegram/telegram
                                    :family.bot.providers.weather.weather-api-com/weather
                                    :family.bot.services.scheduling/weather]))]
       (ig/load-namespaces config [:family.bot.services.messaging.telegram/telegram
                                   :family.bot.providers.weather.weather-api-com/weather
                                   :family.bot.services.scheduling/weather])
       (ig/prep config [:family.bot.services.messaging.telegram/telegram
                        :family.bot.providers.weather.weather-api-com/weather
                        :family.bot.services.scheduling/weather]))))
  ;; integrant repl
  (ig-repl/prep)
  (ig-repl/init)
  (ig-repl/go [:family.bot.services.scheduling/weather])
  (ig-repl/halt)
  (ig-repl/reset)
  (ig-repl/clear)

  ;; observe system & config
  (load-config :dev)
  (identity ig-state/system)

  ;; try weather
  (-> (slurp "http://api.weatherapi.com/v1/forecast.json?q=Kaliningrad&aqi=no&days=1&key=7396fcead4de40c3ab4151221223005&q=Kaliningrad&alerts=no")
      (json/read-value json/keyword-keys-object-mapper))
  (def resp (atom *1))
  (identity @resp)

  (def <m> (get ig-state/system :family.bot.services.messaging.telegram/telegram))
  (identity <m>)

  (telegram/send-message! <m> "test message")


  )