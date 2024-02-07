(ns moody.router
  (:require
   [bidi.bidi :as bidi]
   [pushy.core :as pushy]
   [re-frame.core :as rf]))

(def routes ["/" {"" :home
                  "cards/" :cards
                  "settings/" :settings
                  "conversions/" {"" :conversions
                                  "radix/" :radix
                                  "qr/" :qr
                                  [:input-type "/" :output-type] :conversion}
                  "about/" :about}])

(def history
  (let [dispatch #(rf/dispatch [:route-changed %]) ; NOTE: dispatch-sync?
        match #(bidi/match-route routes %)]
    (pushy/pushy dispatch match)))

(defn start!
  []
  (pushy/start! history))

(def path-for
  (partial bidi/path-for routes))

(defn set-token!
  [token]
  (pushy/set-token! history token))
