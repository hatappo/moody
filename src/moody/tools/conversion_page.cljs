(ns moody.tools.conversion-page
  (:require
   ["@monaco-editor/react" :refer [Editor]]
   [moody.tools.tools :refer [conversion-tools]]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn conversion-page
  []
  ;; [:f>]
  (let [initial-options {:pretty? true}
        options-ratom (r/atom initial-options)
        set-tool-type (fn [tool-type]
                        (rf/dispatch [:set-tool-type tool-type]))
        convert (fn [input-text]
                  (rf/dispatch [:convert input-text @options-ratom]))]
    (fn []
      (let [{:keys [tool-type]} @(rf/subscribe [:nav])
            input-text @(rf/subscribe [:input-text])
            output-text @(rf/subscribe [:output-text])]
        [:article
         [:h2 {:class "text-content1 text-lg my-4"} "Data Format Conversion"]
         [:div {:class "flex items-center gap-6"}
          [:select {:class "select select-ghost-primary"
                    :value tool-type
                    :on-change (fn [e]
                                 (set-tool-type (.. e -target -value))
                                 (convert input-text))}
           (map (fn [{:keys [tool-type label]}] ^{:key tool-type} [:option {:value tool-type} label]) conversion-tools)]
          [:div {:class "flex items-center"}
           [:label {:for "pretty-checkbox"} "pretty?:"]
           [:input {:type "checkbox"
                    :id "pretty-checkbox"
                    :checked (:pretty? @options-ratom)
                    :on-change (fn [_e] (swap! options-ratom update :pretty? not) (convert input-text))
                    :class "switch switch-bordered-primary mx-1"}]]]
         [:section {:class "grid grid-flow-col justify-stretch gap-4"}
          [:div {:class "max-w-full"}
           [:div {:class "my-4"}
            "Input"]
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
            "Output"]
           [:div {:class "max-w-full my-1"}
            [:> Editor {:class ""
                        :height "75vh"
                        :width "95%"
                        :options {:minimap {:enabled false}
                                  :wordWrap "on"
                                  :read-only true}
                        :value output-text
                        :defaultLanguage "clojure"}]]]]]))))
