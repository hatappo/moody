(ns moody.helpers.helpers
  (:require
   [cljs.pprint :refer [pprint]]
   [clojure.set :refer [rename-keys]]
   [clojure.string :as str]))

(defn pprint-str
  [s]
  (with-out-str (pprint s)))

(defn strip-newline-and-tab
  [s]
  (str/replace s #"\r\n|\n|\r|\t" ""))

(defn rename-jsx-specific-attrs-to-html-attrs
  [m]
  (rename-keys m {:classname :class :htmlfor :for}))

(defn determine-data-structure-type
  [text]
  :TODO)

(defn nil->empty [s]
  (if s s ""))
