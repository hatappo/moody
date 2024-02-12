(ns moody.tools.uuid-page
  (:require
   [reagent.core :as r]))

(def initial-values
  [{:format true
    :upper true}])

(defn uuid-page
  []
  (let [format-ratom (r/atom true)
        upper-ratom (r/atom true)]
    (fn []
      [:article {:class "flex flex-col gap-2"}
       [:h2 {:class "text-xl my-4"}
        "UUID Generator"]

       [:section {:class "flex flex-col gap-6 pr-8"}

        [:div {:class "flex items-center gap-2"}
         [:label {:for "format-checkbox"}
          "Format"]
         [:input {:class "switch switch-bordered-primary mr-2"
                  :type "checkbox"
                  :id "format-checkbox"
                  :checked @format-ratom
                  :on-change (fn [_e] (swap! format-ratom not))}]]

        [:div {:class "flex flex-col gap-4"}
         ;;  TODO:
         ]]])))
