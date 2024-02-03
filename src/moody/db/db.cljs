(ns moody.db.db
  (:require
   [day8.re-frame.tracing-stubs :refer-macros [fn-traced]]
   [re-frame.core :as rf]))

(def initial-app-db
  {:nav {:active-page nil}
   :editor {:theme "vs-dark"
            :word-wrap true
            :whitespace-option "all"}
   :conversion {:input-text "<a href=\"/foo/bar#\">hello</a>"
                :output-text "[:a {:href \"/foo/bar#\"} \"hello\"]"}})

(rf/reg-event-db
 :initialize-db
 (fn-traced [_db]
            initial-app-db))
