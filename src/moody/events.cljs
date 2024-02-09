(ns moody.events
  (:require
   [day8.re-frame.tracing-stubs :refer [fn-traced]]
   [moody.tools.tools :refer [convert]]
   [re-frame.core :refer [reg-event-db reg-event-fx]]
   [taoensso.timbre :as timbre]))

(reg-event-db
 :set-options
 (fn-traced [db [event options]]
            (timbre/info {:event event :options options})
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
 :swap-converter
 (fn-traced [{db :db} [event]]
            (let [{:keys [input-type output-type]} (-> db :nav)
                  {:keys [input-text output-text]} (-> db :conversion)]
              (timbre/info {:event event} input-type output-type input-text output-text)
              {:db (-> db
                       (assoc-in [:conversion :input-text] output-text)
                       (assoc-in [:conversion :output-text] input-text))
               :navigate-to {:path (str "/conversions/" (name output-type) "/" (name input-type))}})))

(reg-event-fx
 :navigate-to-conversion-page
 (fn-traced [{db :db} [_ {:keys [route-params]}]]
            (let [{:keys [input-type output-type]} route-params]
              {:db db
               :navigate-to {:path (str "/conversions/" (name input-type) "/" (name output-type))}})))

(reg-event-db
 :set-editor-theme
 (fn-traced [db [_ editor-theme]]
            (assoc-in db [:settings :editor :theme] editor-theme)))

(reg-event-db
 :set-editor-word-wrap
 (fn-traced [db [_ word-wrap]]
            (assoc-in db [:settings :editor :word-wrap] word-wrap)))

(reg-event-db
 :set-editor-whitespace-option
 (fn-traced [db [_ whitespace-option]]
            (assoc-in db [:settings :editor :whitespace-option] whitespace-option)))

(reg-event-db
 :set-input-editor-language
 (fn-traced [db [_ lang]]
            (let [{:keys [input-editor input-monaco]} (:input-monaco-editor db)]
              (when (and input-editor input-monaco)
                (when-let [model (.getModel ^js input-editor)]
                  (.setModelLanguage (.-editor ^js input-monaco) model (name lang)))))))

(reg-event-db
 :set-output-editor-language
 (fn-traced [db [_ lang]]
            (let [{:keys [output-editor output-monaco]} (:output-monaco-editor db)]
              (when (and output-editor output-monaco)
                (when-let [model (.getModel ^js output-editor)]
                  (.setModelLanguage (.-editor ^js output-monaco) model (name lang)))))))

(reg-event-db
 :set-input-monaco-editor-instances
 (fn-traced [db [_ input-editor input-monaco]]
            (assoc db :input-monaco-editor {:input-editor input-editor :input-monaco input-monaco})))

(reg-event-db
 :set-output-monaco-editor-instances
 (fn-traced [db [_ output-editor output-monaco]]
            (assoc db :output-monaco-editor {:output-editor output-editor :output-monaco output-monaco})))
