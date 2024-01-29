(ns moody.feature.hiccup-page
  (:require
   ["@monaco-editor/react" :refer [Editor]]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [taoensso.timbre :as timbre]))

(defn hiccup-page
  []
  ;; [:f>]
  (let [options-ratom (r/atom {:input-data-type "jsx"
                               :pretty? true})
        convert (fn [input-text]
                  (rf/dispatch [:convert input-text @options-ratom]))]
    (fn []
      (let [input-text @(rf/subscribe [:input-text])
            output-text @(rf/subscribe [:output-text])]
        [:article
         [:h2 {:class "text-content1 text-lg mt-4"} "HTML - Hiccup"]
         [:section {:class "grid grid-flow-col justify-stretch gap-4 py-2"}
          [:div {:class "max-w-full"}
           [:div {:class "my-4"}
            "Input"
            [:select {:class "select select-ghost-secondary mx-4"
                      :value (:input-data-type @options-ratom)
                      :on-change (fn [e]
                                   (swap! options-ratom assoc :input-data-type (.. e -target -value))
                                   (convert input-text))}
             [:option {:value "html"} "HTML"]
             [:option {:value "jsx"} "JSX"]]]
           [:div {:class "max-w-full my-1"}
            [:> Editor {:class ""
                        :height "75vh"
                        :width "95%"
                        :options {:minimap {:enabled false}
                                  :wordWrap "on"}
                        #_#_:theme "vs-dark"
                        :value input-text
                        :on-change convert
                        :defaultLanguage "html"}]]]
          [:div {:class "max-w-full"}
           [:div {:class "my-4"}
            "Output"
            [:select {:class "select select-ghost-secondary mx-4"}
             [:option "Hiccup"]]]
           [:div {:class "max-w-full my-1"}
            [:> Editor {:class ""
                        :height "75vh"
                        :width "95%"
                        :options {:minimap {:enabled false}
                                  :wordWrap "on"
                                  :read-only true}
                        :value output-text
                        :defaultLanguage "clojure"}]]]]]))))
