(ns family.bot.services.scheduling.rates
  (:require [immutant.scheduling :as sch]
            [integrant.core :as ig]
            [family.bot
             [providers.rates :as rates]
             [services.messaging.telegram :as telegram]]))

(def ^:private JOB-ID :exchange-rates-scheduler)

(defn- job
  [{<provider> :provider
    <msg> :messaging}]
  (some->> (rates/fetch-rates! <provider>)
           (telegram/send-message! <msg>)))

(defmethod ig/prep-key :family.bot.services.scheduling/rates
  [_ config]
  (assoc-in config [:schedule :id] JOB-ID))

(defmethod ig/init-key :family.bot.services.scheduling/rates
  [_ {:keys [schedule] :as <scheduler>}]
  (sch/schedule #(job <scheduler>) schedule))

(defmethod ig/halt-key! :family.bot.services.scheduling/rates
  [_ scheduler]
  (sch/stop scheduler))
