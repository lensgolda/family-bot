(ns services.messaging.telegram
  (:require [integrant.core :as ig]
            [org.httpkit.client :as http]
            [jsonista.core :as j]))

(def ^:private METHOD-SEND-MESSAGE "sendMessage")

(def emoji
  {:EUR "\uD83C\uDDEA\uD83C\uDDFA"
   :USD "\uD83C\uDDFA\uD83C\uDDF8"
   :RUB "\uD83C\uDDF7\uD83C\uDDFA"
   :right-arrow "➡️"})

(defprotocol Messaging
  (send-message [this message]))

(defrecord Telegram []
  Messaging
  (send-message [{:keys [base-url token chat-id]} message]
    (let [url (format "%s/%s/%s" base-url token METHOD-SEND-MESSAGE)
          body (j/write-value-as-string
                 {:chat_id chat-id
                  :text message
                  :parse_mode "MarkdownV2"}
                 j/keyword-keys-object-mapper)
          request {:url url
                   :method :post
                   :body body
                   :timeout 3000
                   :headers {"Content-Type" "application/json"
                             "Accept" "application/json"}}]
      (http/request request
                    (fn [{:keys [status _headers _body error]}] ;; asynchronous response handling
                      (if error
                        (println "Failed, exception is " error)
                        (println "Async HTTP GET: " status)))))))

(defn send-message!
  [<messaging> message]
  (send-message <messaging> message))

(defmethod ig/init-key :services.messaging/telegram
  [_ config]
  (map->Telegram config))
