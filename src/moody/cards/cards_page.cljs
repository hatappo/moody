(ns moody.cards.cards-page
  (:require
   ["react-icons/bs" :as icons-bs]
   [clojure.string :as str]
   [moody.tools.tools :as tools]
   [reagent.core :as r]
   [taoensso.timbre :as timbre]))

(defn cards-page
  []
  (let [icon?-ratom (r/atom true)
        search-word-ratom (r/atom "")
        search-placeholder-ratom (r/atom (rand-nth ["\"qr\"" "\"html\"" "\"json\"" "\"clojure\"" "\"others\"" "\"radix\"" "\"jwt\""]))]
    (fn []
      [:article {:class "flex flex-col gap-2"}
       [:h2 {:class "text-xl my-4"}
        "All Tools"]
       [:section
        [:div {:class "flex items-center gap-4"}
         [:div {:class "form-control"}
          [:input
           {:class "input input-ghost-primary input-lg pl-11"
            :type "search"
            :id "search-input"
            :value @search-word-ratom
            :placeholder @search-placeholder-ratom
            :on-change #(reset! search-word-ratom (.. % -target -value))}]
          [:span
           {:class "absolute inset-y-0 left-3 inline-flex items-center"}
           [:> icons-bs/BsSearch {:size "1.5em" :class "text-content3"}]]]
         [:span {:class "items-center gap-2 text-neutral"}
          "Type "
          [:kbd {:class "kbd kbd-sm"} "/"]
          " to filter"]]
        [:div {:class "flex flex-wrap my-4 gap-4"}
         (doall
          (->> tools/tools
               (filter (fn [{:keys [relevant-words-text]}]
                         (str/includes? relevant-words-text (str/lower-case @search-word-ratom))))
               (map-indexed
                (fn [_idx {:keys [tool-type path tool-tags icon icon-html title desc]}]
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
                      (->> tool-tags sort (map (fn [tag] ^{:key tag} [:span {:class "badge badge-flat-primary"} (str \# (name tag))])))]]]]))))]
        [:div {:class "flex items-center"}
         [:label {:for "icon-checkbox"} "Icon:"]
         [:input {:type "checkbox"
                  :id "icon-checkbox"
                  :checked @icon?-ratom
                  :on-change #(swap! icon?-ratom not)
                  :class "switch switch-bordered-primary mx-1"}]]]])))

(defn focus-search-input-on-keydown
  [keyboardEvent]
  (timbre/info {:fn :focus-search-input-on-keydown :keyboardEvent keyboardEvent})
  (let [meta-key? (.. keyboardEvent -metaKey) ; `Command` or `Windows` key
        alt-key? (.. keyboardEvent -altKey)   ; `Option` or `Alt` key
        shiftKey? (.. keyboardEvent -shiftKey)
        ctrlKey? (.. keyboardEvent -ctrlKey)
        code (.. keyboardEvent -code)]

    (when (and (not meta-key?) (not alt-key?) (not shiftKey?) (not ctrlKey?) (= code "Slash"))
      (.preventDefault keyboardEvent)
      (.focus (.querySelector js/document "#search-input")))))
