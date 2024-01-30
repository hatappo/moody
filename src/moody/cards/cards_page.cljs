(ns moody.cards.cards-page
  (:require
   [moody.router :as router]
   [moody.tools.tools :as const]
   [reagent.core :as r]))

(defn cards-page
  []
  (let [icon?-ratom (r/atom true)]
    ;; [:f>]
    (fn []
      (let [_ nil]
        [:article
         [:h2 {:class "text-content1 text-lg my-4"} "Tools"]
         [:div {:class "flex items-center"}
          [:label {:for "icon-checkbox"} "Icon:"]
          [:input {:type "checkbox"
                   :id "icon-checkbox"
                   :checked @icon?-ratom
                   :on-change #(swap! icon?-ratom not)
                   :class "switch switch-bordered-primary mx-1"}]]
         [:div {:class "flex flex-wrap my-4 gap-4"}
          (doall
           (map-indexed (fn [_idx {:keys [tool-type tool-category tool-tags icon icon-html title desc]}]
                          ^{:key tool-type}
                          [:a {:href (router/path-for tool-category :tool-type tool-type)}
                           [:div {:class "rounded-md bg-gray-3 h-80 w-40"}
                            [:div {:class "flex flex-col items-center gap-2 p-4"}
                             [:div {:class "flex items-center justify-center text-center bg-gray-5 rounded-md aspect-square w-24 p-2 m-4"}
                              (if @icon?-ratom
                                [:> icon {:size "2em"}]
                                [:span {:dangerouslySetInnerHTML {:__html icon-html}}])]
                             [:h3 {:class "text-content1 text-base"} title]
                             [:p {:class "text-content2 text-sm"} desc]
                             [:div {:class "flex flex-wrap justify-start w-full gap-1"}
                              (->> tool-tags sort (map (fn [tag] ^{:key tag} [:span {:class "badge badge-flat-secondary"} (str \# (name tag))])))]]]])
                        const/tools))]]))))
