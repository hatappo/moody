(ns moody.db.db
  (:require
   [day8.re-frame.tracing-stubs :refer-macros [fn-traced]]
   [re-frame.core :as rf]))

(def initial-app-db
  {:nav {:active-nav nil
         :active-page nil
         :input-type nil
         :output-type nil}
   :editor {:theme "vs-dark"
            :word-wrap true
            :whitespace-option "all"}
   :conversion {:input-text nil
                :output-text nil}})

(rf/reg-event-db
 :initialize-db
 (fn-traced [_db]
            initial-app-db))
