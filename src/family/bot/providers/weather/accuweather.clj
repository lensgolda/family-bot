(ns family.bot.providers.weather.accuweather
  (:require [integrant.core :as ig]
            [family.bot.providers.spec :as providers-spec]))

(defmethod ig/pre-init-spec :family.bot.providers.weather/accuweather
  [_]
  ::providers-spec/accuweather-provider-config)

(defmethod ig/init-key ::weather
  [_ config]
  config)
