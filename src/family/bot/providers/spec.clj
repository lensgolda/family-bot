(ns family.bot.providers.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.string :refer [blank?]]
            [family.bot.spec.common :as spec-common]))

;; basic spec forms
(s/def ::base-url ::spec-common/url)
(s/def ::currency #{"EUR" "USD"})
(s/def ::currencies (s/coll-of ::currency :distinct true :into #{}))
(s/def ::location-key (s/and string? (complement blank?)))
(s/def ::key (s/and string? (complement blank?)))

;; top level spec forms
(s/def ::rates-provider
  (s/keys :req-un [::base-url]
          :opt-un [::currencies]))

(s/def ::accuweather-provider-config
  (s/keys :req-un [::key ::base-url ::location-key]))

(s/def ::weather-api-provider-config
  (s/keys :req-un [::key
                   ::base-url]))