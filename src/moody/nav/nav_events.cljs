(ns moody.nav.nav-events
  (:require
   [clojure.string :as str]
   [day8.re-frame.tracing-stubs :refer-macros [fn-traced]]
   [moody.cards.cards-page :refer [focus-search-input-on-keydown]]
   [moody.router :as router]
   [moody.tools.tools :refer [notations-by-notation-type]]
   [re-frame.core :refer [reg-event-fx reg-fx]]
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

            (let [input-type (keyword (:input-type route-params))
                  output-type (keyword (:output-type route-params))
                  prev-input-type (get-in db [:nav :input-type])
                  db (-> db
                         (assoc-in [:nav :active-page] handler)
                         (assoc-in [:nav :input-type] (keyword input-type))
                         (assoc-in [:nav :output-type] (keyword output-type)))]

              (if (= handler :cards)
                (.addEventListener js/document "keydown" focus-search-input-on-keydown)
                (.removeEventListener js/document "keydown" focus-search-input-on-keydown))

              (case handler
                :home {:db db}
                :cards {:db db}
                :settings {:db db}
                :conversions {:db db}
                :conversion (let [input-notation (input-type notations-by-notation-type)
                                  output-notation (output-type notations-by-notation-type)
                                  input-editor-lang (:editor-lang input-notation)
                                  output-editor-lang (if (= output-type :noop) input-editor-lang (:editor-lang output-notation))
                                  input-text (get-in db [:conversion :input-text])]
                              {:db db
                               :fx [[:dispatch [:set-input-editor-language input-editor-lang]]
                                    [:dispatch [:set-output-editor-language output-editor-lang]]
                                    [:dispatch [:update-input-text (cond
                                                                     (str/blank? input-text) (:example input-notation)
                                                                     (not= prev-input-type input-type) (:example input-notation)
                                                                     :else input-text)]]]})))))

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
