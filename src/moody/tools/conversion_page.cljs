(ns moody.tools.conversion-page
  (:require
   ["@monaco-editor/react" :refer [Editor]]
   ["react-icons/lia" :as icons-lia]
   ["react-icons/lu" :as icons-lu]
   [moody.tools.tools :refer [conversion-tools]]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn conversion-page
  []
  (let [initial-options {:pretty? true}
        options-ratom (r/atom initial-options)
        set-tool-type (fn [tool-type]
                        (rf/dispatch [:set-tool-type tool-type]))
        update-input-text (fn [input-text]
                            (rf/dispatch [:update-input-text input-text @options-ratom]))]
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
                                 (update-input-text input-text))}
           (map (fn [{:keys [tool-type label]}] ^{:key tool-type} [:option {:value tool-type} label]) conversion-tools)]
          [:div {:class "flex items-center"}
           [:label {:for "pretty-checkbox"} "pretty?:"]
           [:input {:type "checkbox"
                    :id "pretty-checkbox"
                    :checked (:pretty? @options-ratom)
                    :on-change (fn [_e] (swap! options-ratom update :pretty? not) (update-input-text input-text))
                    :class "switch switch-bordered-primary mx-1"}]]]
         [:section {:class "grid grid-flow-col justify-stretch gap-4"}
          [:div {:class "max-w-full"}
           [:div {:class "flex items-center w-full"}
            [:div {:class "my-4"}
             "Input"]
            [:div {:class "flex justify-end gap-2 w-full mr-8"}
             [:button {:class "btn btn-solid-primary"
                       :on-click #(update-input-text "")}
              [:> icons-lu/LuEraser {:size "1.5em"}]]
             [:button {:class "btn btn-solid-primary"}
              [:> icons-lia/LiaPasteSolid {:size "1.5em" :class "mr-2"}]
              "Paste"]]]
           [:div {:class "max-w-full my-1"}
            [:> Editor {:class ""
                        :height "75vh"
                        :width "95%"
                        :options {:minimap {:enabled false}
                                  :wordWrap "on"
                                  :autoDetectHighContrast false}
                        :theme "vs-dark"
                        :value input-text
                        :on-change update-input-text
                        :defaultLanguage "html"}]]]
          [:div {:class "max-w-full"}
           [:div {:class "flex items-center w-full"}
            [:div {:class "my-2"}
             [:button {:class "btn btn-solid-primary"}
              [:> icons-lia/LiaCopySolid {:size "1.5em" :class "mr-2"}]
              "Copy"]]
            [:div {:class "flex justify-end w-full mr-10"}
             "Output"]]
           [:div {:class "max-w-full my-1"}
            [:> Editor {:class ""
                        :height "75vh"
                        :width "95%"
                        :options {:minimap {:enabled false}
                                  :wordWrap "on"
                                  :read-only true}
                        :value output-text
                        :defaultLanguage "clojure"}]]]]]))))
