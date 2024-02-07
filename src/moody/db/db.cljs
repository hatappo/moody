(ns moody.db.db
  (:require
   [day8.re-frame.tracing-stubs :refer-macros [fn-traced]]
   [re-frame.core :as rf]))

(def initial-app-db
  {:nav {:active-nav nil
         :active-page nil
         :input-type nil
         :output-type nil}
   :settings {:editor {:theme "vs-dark"
                       :word-wrap true
                       :whitespace-option "all"}}
   :input-monaco-editor {:input-monaco nil
                         :input-editor nil}
   :output-monaco-editor {:output-monaco nil
                          :output-editor nil}
   :conversion {:input-text nil
                :output-text nil}})

(rf/reg-event-db
 :initialize-db
 (fn-traced [_db]
            initial-app-db))
