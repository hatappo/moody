(ns moody.cards.cards-page
  (:require
   [clojure.string :as str]
   [moody.tools.tools :as tools]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn cards-page
  []
  (let [icon?-ratom (r/atom true)]
    (fn []
      (let [search-text @(rf/subscribe [:search-text])
            tools (filter (fn [{:keys [relevant-words-text]}]
                            (str/includes? relevant-words-text (str/lower-case (str search-text))))
                          tools/tools)
            nothing? (zero? (count tools))]
        [:article {:class "flex flex-col gap-2"}
         [:h2 {:class "text-xl my-4"}
          "All Tools"]
         [:section
          [:div {:class "flex flex-wrap my-4 gap-4"}
           (doall
            (->> tools
                 (map-indexed
                  (fn [_idx {:keys [tool-type path tags icon icon-html title desc]}]
                    ^{:key tool-type}
                    [:a {:href path}
                     [:div {:class "rounded-md bg-gray-3 h-80 w-44"}
                      [:div {:class "flex flex-col items-center gap-2 p-4"}
                       [:div {:class "flex items-center justify-center text-center bg-gray-5 rounded-md aspect-square w-24 p-2 m-4"}
                        (if @icon?-ratom
                          [:> icon {:size "2em"}]
                          [:span {:dangerouslySetInnerHTML {:__html icon-html}}])]
                       [:h3 {:class "text-content1 text-base"} title]
                       [:p {:class "text-content2 text-xs"} desc]
                       [:div {:class "flex flex-wrap justify-start w-full gap-1"}
                        (->> tags sort (map (fn [tag] ^{:key tag} [:span {:class "badge badge-flat-primary"} (name tag)])))]]]]))))]
          [:div {:class (str "flex items-center" (when nothing? " hidden"))}
           [:label {:for "icon-checkbox"} "Icon:"]
           [:input {:type "checkbox"
                    :id "icon-checkbox"
                    :checked @icon?-ratom
                    :on-change #(swap! icon?-ratom not)
                    :class "switch switch-bordered-primary mx-1"}]]]]))))
