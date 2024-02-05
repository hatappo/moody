(ns moody.nav.nav-events
  (:require
   [day8.re-frame.tracing-stubs :refer-macros [fn-traced]]
   [moody.cards.cards-page :refer [focus-search-input-on-keydown]]
   [moody.router :as router]
   [re-frame.core :refer [#_path reg-event-fx reg-fx]]
   [taoensso.timbre :as timbre]))

;; (def nav-interceptors [(path :nav)])

(reg-fx
 :navigate-to
 (fn-traced [{:keys [path]}]
            (router/set-token! path)))

(reg-event-fx
 :route-changed
 (fn-traced [{db :db} [event {:keys [handler route-params] :as params}]]
            (timbre/info {:event event :params params})
            (let [db (-> db
                         (assoc-in [:nav :active-page] handler)
                         (assoc-in [:nav :input-type] (keyword (:input-type route-params)))
                         (assoc-in [:nav :output-type] (keyword (:output-type route-params))))]
              (if (= handler :cards)
                (.addEventListener js/document "keydown" focus-search-input-on-keydown)
                (.removeEventListener js/document "keydown" focus-search-input-on-keydown))
              (case handler
                :home {:db db}
                :cards (do (if (= handler :cards)
                             (.addEventListener js/document "keydown" focus-search-input-on-keydown)
                             (.removeEventListener js/document "keydown" focus-search-input-on-keydown))
                           {:db db})
                :conversions {:db db}
                :conversion {:db db
                             :dispatch [:update-input-text (get-in db [:conversion :input-text])]}
                :settings {:db db}))))

;; (reg-event-db
;;  :set-active-nav
;;  nav-interceptors
;;  (fn-traced [nav [_ active-nav]]
;;             (assoc nav :active-nav active-nav)))

;; (reg-event-db
;;  :set-active-page
;;  nav-interceptors
;;  (fn-traced [nav [_ active-page]]
;;             (assoc nav :active-page active-page)))
