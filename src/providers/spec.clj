(ns providers.spec
  (:require [clojure.spec.alpha :as s]))

(s/def ::base-url (s/and string? #(not (clojure.string/blank? %))))
(s/def ::currencies #{"EUR" "USD"})
(s/def ::rates-provider (s/keys :req-un [::base-url]
                                :opt-un [::currencies]))