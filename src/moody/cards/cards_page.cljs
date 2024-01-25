(ns moody.cards.cards-page
  (:require
   [moody.router :as router]
   [re-frame.core :as rf]))

(defn cards-page
  []
  ;; [:f>]
  (fn []
    (let [feature-summaries @(rf/subscribe [:feature-summaries])]
      [:article
       [:h2 {:class "text-content1 text-lg my-4"} "カテゴリ"]
       [:div {:class "flex flex-wrap my-4 gap-4"}
        (map-indexed (fn [_idx {:keys [id icon code-let title desc]}]
                       ^{:key id}
                       [:a {:href (router/path-for id)}
                        [:div {:class "rounded-md bg-gray-3 h-72 w-40"}
                         [:div {:class "flex flex-col items-center gap-2 p-4"}
                          [:div {:class "flex items-center justify-center text-center bg-gray-5 rounded-md aspect-square w-20 p-2 m-4"}
                           (if icon
                             [:> icon {:size "2em"}]
                             [:span code-let])]
                          [:h3 {:class "text-content1 text-base"} title]
                          [:p {:class "text-content2 text-sm"} desc]]]])
                     feature-summaries)
        #_[:div {:class "flex h-48 w-32 items-center justify-center border-2 border-dashed border-border bg-gray-1"} "+"]]])))
