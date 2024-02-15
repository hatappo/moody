(ns moody.nav.nav-events
  (:require
   [clojure.string :as str]
   [day8.re-frame.tracing-stubs :refer-macros [fn-traced]]
   [moody.router :as router]
   [moody.tools.tools :refer [notations-by-notation-type]]
   [re-frame.core :refer [reg-event-db reg-event-fx reg-fx]]
   [re-frame.std-interceptors :refer [path]]
   [taoensso.timbre :as timbre]))

(def nav-interceptors [(path :nav)])

(defn- focus-search-input-on-keydown
  [keyboardEvent]
  (timbre/trace {:fn :focus-search-input-on-keydown :keyboardEvent keyboardEvent})
  (let [meta-key? (.. keyboardEvent -metaKey) ; `Command` or `Windows` key
        alt-key? (.. keyboardEvent -altKey)   ; `Option` or `Alt` key
        shiftKey? (.. keyboardEvent -shiftKey)
        ctrlKey? (.. keyboardEvent -ctrlKey)
        code (.. keyboardEvent -code)]

    (when (and (not meta-key?) (not alt-key?) (not shiftKey?) (not ctrlKey?) (= code "Slash"))
      (when-not (= "search-input" (.. js/document -activeElement -id))
        (.preventDefault keyboardEvent)
        (.focus (.querySelector js/document "#search-input"))))))

(defn- renewEventListenerOfSearchKeydownTrap
  []
  ;; NOTE: To avoid the behavior where event listeners remain duplicated during hot reloading in development,
  ;; removing it once and then adding it again.
  (.removeEventListener js/document "keydown" focus-search-input-on-keydown)
  (.addEventListener js/document "keydown" focus-search-input-on-keydown))

(reg-event-db
 :set-search-text
 nav-interceptors
 (fn-traced [nav [_ search-text]]
            (assoc nav :search-text search-text)))

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
                  {prev-input-type :input-type prev-output-type :output-type} (:nav db)
                  db (-> db
                         (assoc-in [:nav :active-page] handler)
                         (assoc-in [:nav :input-type] (keyword input-type))
                         (assoc-in [:nav :output-type] (keyword output-type))
                         (assoc-in [:nav :search-text] ""))]

              (renewEventListenerOfSearchKeydownTrap)

              (case handler
                :conversion (let [input-notation (input-type notations-by-notation-type)
                                  output-notation (output-type notations-by-notation-type)
                                  input-editor-lang (:editor-lang input-notation)
                                  output-editor-lang (if (= output-type :noop) input-editor-lang (:editor-lang output-notation))
                                  input-text (get-in db [:conversion :input-text])]
                              {:db db
                               :fx [[:dispatch [:set-input-editor-language input-editor-lang]]
                                    [:dispatch [:set-output-editor-language output-editor-lang]]
                                    [:dispatch [:update-input-text (cond
                                                                     ;;  Fill example data when the input is empty.
                                                                     (str/blank? input-text) (:example input-notation)
                                                                     ;;  Swap data output/input when `swap` transition.
                                                                     (and (= prev-input-type output-type) (= prev-output-type input-type)) input-text
                                                                     ;;  Fill example data when input-type is changed
                                                                     (not= prev-input-type input-type) (:example input-notation)
                                                                     ;;  Otherwise, keep it.
                                                                     :else input-text)]]]})
                {:db db}))))

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
