(ns moody.components.button
  (:require
   ["react-icons/lia" :as icons-lia]
   [moody.util :refer [read-from-clipboard-and-show-toast
                       write-to-clipboard-and-show-toast]]))

(defn copy-button
  [get-text-fn]
  (fn []
    [:button {:class "btn"
              :on-click #(write-to-clipboard-and-show-toast (get-text-fn) nil)}
     [:> icons-lia/LiaCopySolid {:size "1.5em" :class "mr-2"}]
     "Copy"]))

(defn paste-button
  []
  (fn [paste-text-fn]
    [:button {:class "btn btn-ghost-primary"
              :on-click #(read-from-clipboard-and-show-toast paste-text-fn)}
     [:> icons-lia/LiaPasteSolid {:size "1.5em" :class "mr-2"}]
     "Paste"]))
