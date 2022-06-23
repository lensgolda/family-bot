(ns family.bot.providers.rates.cbrf
  (:require [clojure.data.zip.xml :refer :all]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [family.bot.providers.spec :as providers-spec]
            [family.bot.services.messaging.telegram :as tg]
            [integrant.core :as ig])
  (:import (java.text DecimalFormat)
           (java.util Locale)))


(defn- iter-zip
  [zipper]
  (->> zipper
       (iterate zip/next)
       (take-while (complement clojure.zip/end?))))

(defn- parse-xml
  [data]
  (let [zipper (-> (.getBytes data "windows-1251")
                   io/input-stream
                   xml/parse
                   zip/xml-zip
                   iter-zip)
        valute? #(-> % zip/node :tag (= :Valute))
        valute->map (fn [v]
                      {:code (xml1-> v (tag= :CharCode) text)
                       :value (xml1-> v (tag= :Value) text)
                       :name (xml1-> v (tag= :Name) text)})
        df (DecimalFormat/getInstance
             (Locale/forLanguageTag "ru-RU"))
        parse-decimal #(.parse df %)
        format-decimal #(.format df %)
        value->decimal-str #(update % :value (comp format-decimal parse-decimal))
        process-valute (comp
                         (filter valute?)
                         (map valute->map)
                         (map value->decimal-str))]
    (.setMaximumFractionDigits df 2)
    (transduce process-valute conj [] zipper)))

(defn- fetch-rates*
  [{:keys [currencies base-url]}]
  (try
    (when (seq currencies)
      (when-let [response (slurp base-url)]
        (let [valutes (parse-xml response)]
          (for [c currencies
                v valutes
                :when (and (seq valutes) (= c (:code v)))]
            v))))
    (catch Exception e
      (log/error e "error fetching rates"))))

(defn- rates-map->strings
  [rates]
  (for [r rates
        :let [k1 (-> r :code keyword)
              k2 (keyword "RUB")
              v (:value r)]]
    (format "\uD83D\uDCB1 %s %s %s\n" (tg/emoji k1) v (tg/emoji k2))))

(defn- format-rates
  [rates]
  (as-> rates $
    (rates-map->strings $)
    (apply str "*Курсы валют*\n" $)))

(defrecord Cbrf []
  family.bot.providers.rates/RatesFetching
  (fetch-rates [this]
    (some-> (fetch-rates* this)
            (format-rates))))

(defmethod ig/pre-init-spec :family.bot.providers.rates/cbrf
  [_]
  ::providers-spec/rates-provider)

(defmethod ig/init-key :family.bot.providers.rates/cbrf
  [_ config]
  (map->Cbrf config))