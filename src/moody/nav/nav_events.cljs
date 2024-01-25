(ns moody.nav.nav-events
  (:require
   [day8.re-frame.tracing-stubs :refer-macros [fn-traced]]
   [moody.router :as router]
   [re-frame.core :refer [path reg-event-db reg-event-fx reg-fx]]))

(def nav-interceptors [(path :nav)])

(reg-fx
 :navigate-to
 (fn-traced [{:keys [path]}]
            (router/set-token! path)))

(reg-event-fx
 :route-changed
 nav-interceptors
 (fn-traced [{nav :db} [_ {:keys [handler route-params]}]]
            (let [nav (assoc nav :active-page handler)]
              (case handler
                :home {:db nav}
                :hoge {:db nav}
                :cards {:db nav}
                :settings {:db nav}
                :hiccup {:db nav}

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
