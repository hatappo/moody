(ns moody.events
  (:require
   [day8.re-frame.tracing-stubs :refer [fn-traced]]
   [moody.tools.tools :refer [convert]]
   [re-frame.core :refer [reg-event-db reg-event-fx]]
   [taoensso.timbre :as timbre]))

(reg-event-db
 :set-options
 (fn-traced [db [_ options]]
            (timbre/info {:event :set-options :options options})
            (assoc-in db [:conversion :options] options)))

(reg-event-db
 :update-input-text
 (fn-traced [db [event input-text options]]
            (let [{:keys [input-type output-type] :as nav} (-> db :nav)]
              (timbre/trace {:event event :input-text input-text})
              (timbre/info {:event event :nav nav :options options})
              (let [output-text (convert input-text (merge options {:input-type input-type :output-type output-type}))]
                (-> db
                    (assoc-in [:conversion :input-text] input-text)
                    (assoc-in [:conversion :output-text] output-text))))))

(reg-event-fx
 :navigate-to-conversion-page
 (fn-traced [{db :db} [_ {:keys [route-params]}]]
            (let [{:keys [input-type output-type]} route-params]
              {:db db
               :navigate-to {:path (str "/conversions/" (name input-type) "/" (name output-type))}})))

(reg-event-db
 :set-editor-theme
 (fn-traced [db [_ editor-theme]]
            (assoc-in db [:editor :theme] editor-theme)))

(reg-event-db
 :set-editor-word-wrap
 (fn-traced [db [_ word-wrap]]
            (assoc-in db [:editor :word-wrap] word-wrap)))

(reg-event-db
 :set-editor-whitespace-option
 (fn-traced [db [_ whitespace-option]]
            (assoc-in db [:editor :whitespace-option] whitespace-option)))