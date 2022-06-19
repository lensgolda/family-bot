(ns services.scheduling.weather
  (:require [integrant.core :as ig]
            [immutant.scheduling :as sch]
            [providers.weather :as weather]
            [services.messaging.telegram :as telegram]))


(def ^:private JOB-ID :weather-fetching-scheduler)

(defn- job
  [{<provider> :provider
    <msg> :messaging}]
  (some->> (weather/fetch-weather! <provider>)
           (telegram/send-message! <msg>)))

(defmethod ig/prep-key :services.scheduling/weather
  [_ config]
  (assoc-in config [:schedule :id] JOB-ID))

(defmethod ig/init-key :services.scheduling/weather
  [_ {:keys [schedule] :as <scheduler>}]
  (sch/schedule #(job <scheduler>) schedule))

(defmethod ig/halt-key! :services.scheduling/weather
  [_ scheduler]
  (sch/stop scheduler))