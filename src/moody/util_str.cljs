(ns moody.util-str
  (:require
   [cljs.pprint :refer [pprint]]
   [clojure.string :as str]))

(defn pprint-str
  [s]
  (with-out-str (pprint s)))

(defn strip-newline-and-tab
  [s]
  (str/replace s #"\r\n|\n|\r|\t" ""))

(defn slice
  [s begin end]
  (when (string? s)
    (.slice s begin end)))

(defn pad
  ([s length padding]
   (pad s length padding :left))
  ([s length padding position]
   (let [padding (slice padding 0 1)
         pad-len (- length (count s))
         pad-len (if (neg? pad-len) 0 pad-len)]
     (case position
       :right (str s (apply str (repeat padding pad-len)))
       (str (apply str (repeat pad-len padding)) s)))))
