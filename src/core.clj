(ns core
  (:require [integrant.core :as ig]
            [aero.core :as aero])
  (:gen-class))

(defmethod aero.core/reader 'ig/ref
  [_ _ value]
  (ig/ref value))

(defn -main
  [& _]
  (-> (aero/read-config "config/prod.edn" {:profile :prod})
      (ig/init)))