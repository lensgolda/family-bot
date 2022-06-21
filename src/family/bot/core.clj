(ns family.bot.core
  (:require [integrant.core :as ig]
            [aero.core :as aero]
            [clojure.string :as str]
            [clojure.tools.logging :as log])
  (:gen-class))

(defmethod aero/reader 'ig/ref
  [_ _ value]
  (ig/ref value))

(defn -main
  [& args]
  (log/info ">>> start family-bot with profile **" (first args) "**")
  (let [profile (some-> (first args)
                        str/trim
                        str/lower-case
                        keyword
                        (or :prod))
        config (aero/read-config "config/example.dev.edn" {:profile profile})]
    (ig/load-namespaces config
      [:family.bot.services.scheduling/rates :family.bot.services.scheduling/weather])
    (ig/init config
      [:family.bot.services.scheduling/rates :family.bot.services.scheduling/weather])))