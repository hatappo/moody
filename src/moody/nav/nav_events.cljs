(ns moody.nav.nav-events
  (:require
   [day8.re-frame.tracing-stubs :refer-macros [fn-traced]]
   [moody.router :as router]
   [re-frame.core :refer [path reg-event-db reg-event-fx reg-fx]]
   [taoensso.timbre :as timbre]))

(def nav-interceptors [(path :nav)])

(reg-fx
 :navigate-to
 (fn-traced [{:keys [path]}]
            (router/set-token! path)))

(reg-event-fx
 :route-changed
 nav-interceptors
 (fn-traced [{nav :db} [_ {:keys [handler route-params] :as params}]]
            (timbre/info {:event :route-changed :params params})
            (let [nav (-> nav
                          (assoc :active-page handler)
                          (assoc :tool-type (:tool-type route-params)))]
              (case handler
                :home {:db nav}
                :cards {:db nav}
                :conversions {:db nav}
                :conversion {:db nav}
                :settings {:db nav}
                :inbox
                {:db (assoc nav :active-inbox (keyword (:inbox-id route-params)))}))))

(reg-event-db
 :set-active-nav
 nav-interceptors
 (fn-traced [nav [_ active-nav]]
            (assoc nav :active-nav active-nav)))

(reg-event-db
 :set-active-page
 nav-interceptors
 (fn-traced [nav [_ active-page]]
            (assoc nav :active-page active-page)))

(reg-event-db
 :set-tool-type
 nav-interceptors
 (fn-traced [nav [_ tool-type]]
            (assoc nav :tool-type tool-type)))
