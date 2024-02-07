(ns moody.tools.conversion-page
  (:require
   ["@monaco-editor/react" :refer [Editor]]
   ["react-icons/go" :as icons-go]
   ["react-icons/lia" :as icons-lia]
   ["react-icons/lu" :as icons-lu]
   [clojure.string :as str]
   [moody.tools.editor :refer [themes word-wrap-val]]
   [moody.tools.tools :refer [notations notations-by-notation-type]]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn conversion-page
  []
  (let [initial-options {:pretty? true}
        options-ratom (r/atom initial-options)
        input-editor-ratom (r/atom nil)
        input-monaco-ratom (r/atom nil)
        output-editor-ratom (r/atom nil)
        output-monaco-ratom (r/atom nil)
        navigate-to-conversion-page (fn [input-type output-type]
                                      (rf/dispatch [:navigate-to-conversion-page {:handler :conversion
                                                                                  :route-params {:input-type input-type
                                                                                                 :output-type output-type}}]))
        set-editor-theme (fn [editor-theme]
                           (rf/dispatch [:set-editor-theme editor-theme]))
        update-input-text (fn [input-text]
                            (rf/dispatch [:update-input-text input-text @options-ratom]))]
    (fn []
      (let [editor-theme @(rf/subscribe [:editor-theme])
            editor-word-wrap @(rf/subscribe [:editor-word-wrap])
            editor-whitespace-option @(rf/subscribe [:editor-whitespace-option])
            input-text @(rf/subscribe [:input-text])
            output-text @(rf/subscribe [:output-text])
            {:keys [input-type output-type]} @(rf/subscribe [:nav])
            input-notation (notations-by-notation-type input-type)
            output-notation (notations-by-notation-type output-type)]
        [:article {:class "flex flex-col gap-2"}
         [:h2 {:class "text-xl my-4"}
          "Data Format Conversion"]

         [:section
          [:div {:class "flex items-center gap-2"}
           [:span "from"]
           [:select {:class "select select-ghost-primary w-44"
                     :value input-type
                     :on-change (fn [e]
                                  (let [input-type (keyword (.. e -target -value))
                                        input-notation (notations-by-notation-type input-type)]
                                    (.setModelLanguage (.-editor ^js @input-monaco-ratom) (.getModel ^js @input-editor-ratom) (:editor-lang input-notation))
                                    (.setModelLanguage (.-editor ^js @output-monaco-ratom) (.getModel ^js @output-editor-ratom) (:editor-lang input-notation))
                                    (navigate-to-conversion-page input-type (first (keys (:convert-fns input-notation))))
                                    (when (str/blank? input-text) (update-input-text (:example input-notation)))))}
            (->> notations
                 (filter (fn [{:keys [convert-fns]}] (pos? (count convert-fns))))
                 (map (fn [{:keys [notation-type label]}] ^{:key notation-type} [:option {:value notation-type} label])))]
           [:span "to"]
           [:select {:class "select select-ghost-primary w-44"
                     :value output-type
                     :on-change (fn [e]
                                  (let [output-type (keyword (.. e -target -value))
                                        output-notation (notations-by-notation-type output-type)
                                        editor-lang (if (= output-type :noop) (:editor-lang input-notation) (:editor-lang output-notation))]
                                    (.setModelLanguage (.-editor ^js @output-monaco-ratom) (.getModel ^js @output-editor-ratom) editor-lang)
                                    (navigate-to-conversion-page input-type output-type)))}
            (let [{:keys [convert-fns]} (notations-by-notation-type (keyword input-type))]
              (->> notations
                   (filter (fn [{:keys [notation-type]}] (notation-type convert-fns)))
                   (map (fn [{:keys [notation-type label]}] ^{:key notation-type} [:option {:value notation-type} label]))))]
           [:span {:class "tooltip tooltip-top", :data-tooltip "Swap 'from' and  'to'"}
            [:button {:class "btn btn-ghost"
                      :on-click #()}
             [:> icons-go/GoArrowSwitch]]]]]

         [:section {:class "grid grid-flow-col justify-stretch gap-4 "}
          [:div {:class "max-w-full"}
           [:div {:class "flex items-center my-2"}
            [:div {:class "ml-1"}
             "Input"]
            [:div {:class "flex justify-end gap-2 w-full mr-1"}
             [:button {:class "btn btn-solid-primary"
                       :on-click #(update-input-text "")}
              [:> icons-lu/LuEraser {:size "1.5em"}]]
             [:button {:class "btn btn-solid-primary"}
              [:> icons-lia/LiaPasteSolid {:size "1.5em" :class "mr-2"}]
              "Paste"]]]
           [:div {:class "flex justify-center max-w-full"}
            [:> Editor {:height "70vh"
                        :width "99%"
                        :options {:minimap {:enabled false}
                                  :wordWrap (word-wrap-val editor-word-wrap)
                                  :renderWhitespace editor-whitespace-option
                                  :autoDetectHighContrast false}
                        #_#_:theme "vs-dark" ; なぜか効かないので on-mount で同様の設定を行っている
                        :value input-text
                        :on-change update-input-text
                        :on-mount (fn [editor, monaco]
                                    (.setTheme (.-editor ^js monaco) editor-theme)
                                    (.setModelLanguage (.-editor ^js monaco) (.getModel ^js editor) (:editor-lang input-notation))
                                    (reset! input-editor-ratom editor)
                                    (reset! input-monaco-ratom monaco))}]]]
          [:div {:class "max-w-full"}
           [:div {:class "flex items-center my-2"}
            [:div {:class "flex gap-2 w-full"}
             [:button {:class "btn btn-solid-primary"}
              [:> icons-lia/LiaCopySolid {:size "1.5em" :class "mr-2"}]
              "Copy"]
             [:span
              {:class "tooltip tooltip-top", :data-tooltip "Paste the text on the clipboard to overwrite it with the conversion result."}
              [:button {:class "btn btn-solid-primary"}
               [:> icons-lia/LiaPasteSolid {:size "1.5em" :class "mr-1"}]
               "&"
               [:> icons-lia/LiaCopySolid {:size "1.5em" :class "ml-1"}]]]]
            [:div {:class "flex items-center gap-2 justify-end w-full"}
             [:label {:for "pretty-checkbox"}
              "no-prettify"]
             [:input {:class "switch switch-bordered-primary mr-2"
                      :type "checkbox"
                      :id "pretty-checkbox"
                      :checked (not (:pretty? @options-ratom))
                      :on-change (fn [_e] (swap! options-ratom update :pretty? not) (update-input-text input-text))}]]]

           [:div {:class "max-w-full"}
            [:> Editor {:height "70vh"
                        :width "99%"
                        :options {:minimap {:enabled false}
                                  :wordWrap (word-wrap-val editor-word-wrap)
                                  :read-only true}
                        :value (str output-text)
                        :on-mount (fn [editor, monaco]
                                    (.setModelLanguage (.-editor ^js monaco) (.getModel ^js editor) (:editor-lang output-notation))
                                    (reset! output-editor-ratom editor)
                                    (reset! output-monaco-ratom monaco))}]]]]
         [:section
          [:span]]]))))
