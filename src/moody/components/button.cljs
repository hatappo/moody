(ns moody.components.button
  (:require
   ["react-icons/lia" :as icons-lia]
   [moody.util :refer [read-from-clipboard-and-show-toast
                       write-to-clipboard-and-show-toast]]))

(defn copy-button
  ([s]
   (copy-button s "Copy"))
  ([s label]
   [:button {:class "btn btn-sm gap-2"
             :title "Copy"
             :on-click #(write-to-clipboard-and-show-toast s nil)}
    [:> icons-lia/LiaCopySolid {:size "1.3em"}]
    label]))

(defn paste-button
  ([paste-text-fn]
   (paste-button paste-text-fn "Paste"))
  ([paste-text-fn label]
   [:button {:class "btn btn-sm btn-ghost-primary gap-2"
             :title "Paste"
             :on-click #(read-from-clipboard-and-show-toast paste-text-fn)}
    [:> icons-lia/LiaPasteSolid {:size "1.3em"}]
    label]))
