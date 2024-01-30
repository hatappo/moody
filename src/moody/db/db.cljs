(ns moody.db.db
  (:require
   [day8.re-frame.tracing-stubs :refer-macros [fn-traced]]
   [re-frame.core :as rf]))

(def initial-app-db
  {:conversion {:options {:pretty? true}
                :input-text "<a href=\"/foo/bar#\">hello</a>"
                :output-text "[:a {:href \"/foo/bar#\"} \"hello\"]"}
   :nav {:active-page nil}})

(rf/reg-event-db
 :initialize-db
 (fn-traced [_db]
            initial-app-db))
