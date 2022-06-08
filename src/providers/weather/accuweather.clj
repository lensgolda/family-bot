(ns providers.weather.accuweather
  (:require [integrant.core :as ig]))

(defmethod ig/init-key :providers.weather/accuweather
  [_ config]
  config)
