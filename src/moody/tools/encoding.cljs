(ns moody.tools.encoding
  (:require
   [clojure.string :as str]))

(def text-encoder (js/TextEncoder.))

(defn base64-encode
  [s]
  (->> s
       (.encode text-encoder)
       (map #(.fromCharCode js/String %))
       (str/join)
       (js/btoa)))

(def text-decoder (js/TextDecoder.))

(defn base64-decode
  [s]
  (try
    (as-> s $
          (js/atob $)
          (.from js/Uint8Array $ (fn [b] (.charCodeAt b 0)))
          (.decode text-decoder $))
    (catch js/Error _e nil)))
