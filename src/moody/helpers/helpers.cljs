(ns moody.helpers.helpers
  (:require
   [cljs.pprint :refer [pprint]]
   [clojure.set :refer [rename-keys]]
   [clojure.string :as str]
   [edamame.core :refer [parse-string]]
   [sql-formatter :as sql-formatter]))

(defn nil->empty [s]
  (if s s ""))

(defn pprint-str
  [s]
  (with-out-str (pprint s)))

(defn strip-newline-and-tab
  [s]
  (str/replace s #"\r\n|\n|\r|\t" ""))

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
  (cond
    (html? s) :html
    (json? s) :json
    (clj? s) :clj
    (sql? s) :sql
    :else :unknown))
