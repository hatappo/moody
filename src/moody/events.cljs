(ns moody.events
  (:require
   [clojure.string :as str]
   [clojure.walk :refer [prewalk]]
   [day8.re-frame.tracing-stubs :refer [fn-traced]]
   [hickory.core :refer [as-hiccup as-hickory parse-fragment]]
   [hickory.render :refer [hickory-to-html]]
   [moody.helpers.str-helpers :refer [pprint-str
                                      rename-jsx-specific-attrs-to-html-attrs
                                      strip-newline-and-tab]]
   [re-frame.core :refer [reg-event-db]]
   [taoensso.timbre :as timbre]))

(defn- convert-jsx-to-html
  [jsx]
  (->> jsx
       strip-newline-and-tab
       parse-fragment
       (map as-hickory)
       (prewalk (fn [node] (if (map? node) (rename-jsx-specific-attrs-to-html-attrs node) node)))
       (map hickory-to-html)
       (str/join "")))

(defn- convert-html-to-hiccup
  [html {:keys [pretty?]}]
  (->> html
       strip-newline-and-tab
       parse-fragment
       (map as-hiccup)
       (#(if pretty? (map pprint-str %) (map pr-str %)))
       (str/join "")))

(defn- convert-jsx-to-hiccup
  [jsx options]
  (-> jsx
      convert-jsx-to-html
      (convert-html-to-hiccup options)))

(reg-event-db
 :convert
 (fn-traced [db [_ input-text {:keys [input-data-type] :as options}]]
            (timbre/trace "input-text:" input-text)
            (timbre/info "options:" options)
            (let [output-text (case input-data-type
                                "html" (convert-html-to-hiccup input-text options)
                                "jsx" (convert-jsx-to-hiccup input-text options)
                                "error: unexpected input-data-type")]
              (-> db
                  (assoc-in [:conversion :input-text] input-text)
                  (assoc-in [:conversion :output-text] output-text)))))
