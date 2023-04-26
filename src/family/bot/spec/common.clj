(ns family.bot.spec.common
  (:require [clojure.spec.alpha :as s]
            [clojure.string :refer [blank?]]))

(s/def ::url (s/and string? #(not (blank? %))))