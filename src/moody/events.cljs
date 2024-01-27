(ns moody.events
  (:require
   [cljs.pprint :refer [pprint]]
   [clojure.string :refer [replace]]
   [day8.re-frame.tracing-stubs :refer [fn-traced]]
   [hickory.core :refer [as-hiccup parse-fragment]]
   [re-frame.core :refer [reg-event-db]]))

(defn pprint-str [s]
  (with-out-str (pprint s)))

(defn strip-newline-and-tab [s]
  (replace s #"\r\n|\n|\r|\t" ""))

(defn nil->empty [s]
  (if s s ""))

(reg-event-db
 :convert-html-to-hiccup
 (fn-traced [db [_ input-text {:keys [pretty?]}]]
            (js/console.log pretty?)
            (let [output (->> input-text
                              strip-newline-and-tab
                              parse-fragment
                              (map as-hiccup)
                              #_(map pprint-str)
                              (#(if pretty? (map pprint-str %) (map pr-str %)))
                              (clojure.string/join ""))]
              (assoc-in db [:conversion :output-text] output))))
