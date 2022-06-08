(ns spec.common
  (:require [clojure.spec.alpha :as s]))

(s/def ::url (s/and string? #(not (clojure.string/blank? %))))