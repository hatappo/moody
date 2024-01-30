(ns moody.events
  (:require
   [clojure.string :as str]
   [clojure.walk :refer [prewalk]]
   [day8.re-frame.tracing-stubs :refer [fn-traced]]
   [hickory.core :refer [as-hiccup as-hickory parse-fragment]]
   [hickory.render :refer [hickory-to-html]]
   [moody.helpers.helpers :refer [pprint-str
                                  rename-jsx-specific-attrs-to-html-attrs
                                  strip-newline-and-tab]]
   [re-frame.core :refer [reg-event-db]]
   [taoensso.timbre :as timbre]))

(reg-event-db
 :set-options
 (fn-traced [db [_ options]]
            (timbre/info {:event :set-options :options options})
            (assoc-in db [:conversion :options] options)))

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
       (map (if pretty? pprint-str pr-str))
       (str/join "")))

(defn- convert-jsx-to-hiccup
  [jsx options]
  (-> jsx
      convert-jsx-to-html
      (convert-html-to-hiccup options)))

(reg-event-db
 :convert
 (fn-traced [db [_ input-text]]
            (let [options (-> db :conversion :options)
                  {:keys [tool-type] :as nav} (-> db :nav)]
              (timbre/trace {:event :convert :input-text input-text})
              (timbre/info {:event :convert :nav nav :options options})
              (let [output-text (case tool-type
                                  "" "(select input data type such as 'HTML', 'JSX', etc.)"
                                  "html2hiccup" (convert-html-to-hiccup input-text options)
                                  "jsx2hiccup" (convert-jsx-to-hiccup input-text options)
                                  "(error: unexpected input-data-type)")]
                (-> db
                    (assoc-in [:conversion :input-text] input-text)
                    (assoc-in [:conversion :output-text] output-text))))))
