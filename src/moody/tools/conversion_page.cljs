(ns moody.tools.conversion-page
  (:require
   ["@monaco-editor/react" :refer [Editor]]
   ["react-icons/lia" :as icons-lia]
   ["react-icons/lu" :as icons-lu]
   [moody.tools.editor :refer [themes]]
   [moody.tools.tools :refer [conversion-tools]]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn conversion-page
  []
  (let [initial-options {:pretty? true :input-lang "plaintext" :output-lang "plaintext"}
        options-ratom (r/atom initial-options)
        input-editor-ratom (r/atom nil)
        input-monaco-ratom (r/atom nil)
        output-editor-ratom (r/atom nil)
        output-monaco-ratom (r/atom nil)
        set-tool-type (fn [tool-type]
                        (rf/dispatch [:set-tool-type tool-type]))
        set-editor-theme (fn [editor-theme]
                           (rf/dispatch [:set-editor-theme editor-theme]))
        update-input-text (fn [input-text]
                            (rf/dispatch [:update-input-text input-text @options-ratom]))]
    (fn []
      (let [{:keys [tool-type]} @(rf/subscribe [:nav])
            editor-theme @(rf/subscribe [:editor-theme])
            input-text @(rf/subscribe [:input-text])
            output-text @(rf/subscribe [:output-text])]
        [:article
         [:section {:class "p-2"}
          [:h2 {:class "text-content1 text-lg my-2"} "Data Format Conversion"]
          [:div {:class "flex items-center gap-6"}
           [:select {:class "select select-ghost-primary"
                     :value tool-type
                     :on-change (fn [e]
                                  (set-tool-type (.. e -target -value))
                                  (update-input-text input-text))}
            (map (fn [{:keys [tool-type label]}] ^{:key tool-type} [:option {:value tool-type} label]) conversion-tools)]]]
         [:section {:class "grid grid-flow-col justify-stretch gap-4 p-2"}
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
            [:> Editor {:height "75vh"
                        :width "99%"
                        :options {:minimap {:enabled false}
                                  :wordWrap "on"
                                  :autoDetectHighContrast false}
                        :theme "vs-dark" ; なぜか効かないので on-mount で同様の設定を行っている
                        :value input-text
                        :on-change update-input-text
                        :on-mount (fn [editor, monaco]
                                    (.setTheme (.-editor ^js monaco) editor-theme)
                                    ;; (.setModelLanguage (.-editor monaco) (.getModel editor) "html")
                                    (reset! input-editor-ratom editor)
                                    (reset! input-monaco-ratom monaco))
                        :defaultLanguage "html"}]]]
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
            [:div {:class "flex items-center justify-end w-full"}
             [:label {:for "pretty-checkbox"} "no-prettify"]
             [:input {:type "checkbox"
                      :id "pretty-checkbox"
                      :checked (not (:pretty? @options-ratom))
                      :on-change (fn [_e] (swap! options-ratom update :pretty? not) (update-input-text input-text))
                      :class "switch switch-bordered-primary mx-1"}]]]

           [:div {:class "max-w-full"}
            [:> Editor {:height "75vh"
                        :width "99%"
                        :options {:minimap {:enabled false}
                                  :wordWrap "on"
                                  :read-only true}
                        :value (str output-text)
                        :on-mount (fn [editor, monaco]
                                    (reset! output-editor-ratom editor)
                                    (reset! output-monaco-ratom monaco))
                        :defaultLanguage "clojure"}]]]]
         [:section {:class "p-2"}
          [:select {:class "select select-ghost-primary"
                    :value editor-theme
                    :on-change (fn [e]
                                 (let [a (.. e -target -value)]
                                   (set-editor-theme a)
                                   ;;  JS オブジェクトなのでリアクティブに更新されない。そのため明示的に更新。
                                   (.setTheme (.-editor ^js @input-monaco-ratom) a)))}
           (map (fn [{:keys [theme-name]}] ^{:key theme-name} [:option {:value theme-name} theme-name]) themes)]]]))))
