(ns moody.util
  (:require
   [clojure.set :refer [rename-keys]]
   [clojure.string :as str]
   [edamame.core :refer [parse-string]]
   [moody.components.toast :refer [show-toast-success]]
   [sql-formatter :as sql-formatter]))

(defn rename-jsx-specific-attrs-to-html-attrs
  [m]
  (rename-keys m {:classname :class :htmlfor :for}))

(defn html?
  [s]
  (let [ss (str/trim s)]
    (and (str/starts-with? ss "<") (str/ends-with? ss ">"))))

(defn json?
  [s]
  (try (.parse js/JSON s) true
       (catch js/Error _e false)))

(defn clj?
  [s]
  (try (parse-string s) true
       (catch js/Error _e false)))

(defn sql?
  [s]
  (try (sql-formatter/format s) true
       (catch js/Error _e false)))

(defn determine-data-format
  [s]
  (cond ; TODO:
    (html? s) :html
    (json? s) :json
    (clj? s) :clj
    (sql? s) :sql
    :else :unknown))

(defn write-to-clipboard
  [s then-fn]
  (.then
   (.. js/navigator -clipboard (writeText s))
   #(when then-fn (then-fn))))

(defn read-from-clipboard
  [then-fn]
  (.then
   (.. js/navigator -clipboard (readText))
   #(when then-fn (then-fn %))))

(defn write-to-clipboard-and-show-toast
  ([s then-fn]
   (write-to-clipboard-and-show-toast s then-fn {}))
  ([s then-fn toast-options]
   (.then
    (.. js/navigator -clipboard (writeText s))
    (fn []
      (when then-fn (then-fn))
      (write-to-clipboard s #(show-toast-success (merge {:text "Copied 👍 "} toast-options)))))))

(defn read-from-clipboard-and-show-toast
  ([then-fn]
   (read-from-clipboard-and-show-toast then-fn {}))
  ([then-fn toast-options]
   (.then
    (.. js/navigator -clipboard (readText))
    (fn [text]
      (when then-fn (then-fn text))
      (read-from-clipboard #(show-toast-success (merge {:text "Pasted 👍 "} toast-options)))))))
