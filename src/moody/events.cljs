(ns moody.events
  (:require
   [day8.re-frame.tracing-stubs :refer [fn-traced]]
   [moody.tools.tools :refer [convert]]
   [re-frame.core :refer [reg-event-db]]
   [taoensso.timbre :as timbre]))

(reg-event-db
 :set-options
 (fn-traced [db [_ options]]
            (timbre/info {:event :set-options :options options})
            (assoc-in db [:conversion :options] options)))

(reg-event-db
 :convert
 (fn-traced [db [_ input-text options]]
            (let [{:keys [tool-type] :as nav} (-> db :nav)]
              (timbre/trace {:event :convert :input-text input-text})
              (timbre/info {:event :convert :nav nav :options options :tool-type tool-type})
              (let [output-text (convert input-text options tool-type)]
                (-> db
                    (assoc-in [:conversion :input-text] input-text)
                    (assoc-in [:conversion :output-text] output-text))))))
