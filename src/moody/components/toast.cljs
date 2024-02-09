(ns moody.components.toast
  (:require
   ["toastify-js" :as toastify]
   [clojure.set :refer [union]]
   [clojure.string :as str]))

(def default-options
  {:text "Copied"
   :duration 4000
   :close true
   :oldestFirst false ; <- not effective
   :position "" ; <- not effective
   :selector "main-container"
   :offset {:x 10 :y 10}
   :className "opacity-95 rounded-lg z-10 m-4 py-2 px-4 absolute right-0"})

(defn- merge-options
  [this that]
  (let [this-classes (set (str/split (:className this) " "))
        that-classes (set (str/split (:className that) " "))]
    (->> (union this-classes that-classes)
         (str/join " ")
         (array-map :className)
         (merge this that))))

(defn- show-toast
  [options]
  (.showToast (toastify (clj->js (merge-options default-options options)))))

(defn show-toast-success
  ([]
   (show-toast-success {}))
  ([options]
   (show-toast (merge-options {:className "moody-bg-gradient-success"} options))))

(defn show-toast-error
  ([]
   (show-toast-success {}))
  ([options]
   (show-toast (merge-options {:className "moody-bg-gradient-error"} options))))
