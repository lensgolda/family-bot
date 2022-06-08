(ns spec.common
  (:require [clojure.spec.alpha :as s]))

(s/def ::base-url (s/and string? #(not (clojure.string/blank? %))))
(s/def ::currencies #{"EUR" "USD"})
(s/def ::provider-rates (s/keys :req-un [::base-url]
                                :opt-un [::currencies]))