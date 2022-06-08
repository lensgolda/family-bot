(ns providers.weather.accuweather
  (:require [integrant.core :as ig]
            [providers.spec :as providers-spec]))

(defmethod ig/pre-init-spec :providers.weather/accuweather
  [_]
  ::providers-spec/accuweather-provider-config)

(defmethod ig/init-key :providers.weather/accuweather
  [_ config]
  config)
