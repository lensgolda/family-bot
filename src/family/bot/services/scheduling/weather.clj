(ns family.bot.services.scheduling.weather
  (:require [integrant.core :as ig]
            [immutant.scheduling :as sch]
            [family.bot.providers.weather :as weather]
            [family.bot.services.messaging.telegram :as telegram]))


(def ^:private JOB-ID :weather-fetching-scheduler)

(defn- job
  [{<provider> :provider
    <msg> :messaging}]
  (some->> (weather/fetch-weather! <provider>)
           (telegram/send-message! <msg>)))

(defmethod ig/prep-key :family.bot.services.scheduling/weather
  [_ config]
  (assoc-in config [:schedule :id] JOB-ID))

(defmethod ig/init-key :family.bot.services.scheduling/weather
  [_ {:keys [schedule] :as <scheduler>}]
  (sch/schedule #(job <scheduler>) schedule))

(defmethod ig/halt-key! :family.bot.services.scheduling/weather
  [_ scheduler]
  (sch/stop scheduler))