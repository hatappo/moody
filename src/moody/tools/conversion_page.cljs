(ns moody.tools.conversion-page
  (:require
   ["@monaco-editor/react" :refer [Editor]]
   ["react-icons/go" :as icons-go]
   ["react-icons/lia" :as icons-lia]
   ["react-icons/lu" :as icons-lu]
   [moody.components.button :refer [copy-button paste-button]]
   [moody.tools.editor :refer [word-wrap-val]]
   [moody.tools.tools :refer [convert notations
                              notations-by-notation-type]]
   [moody.util :refer [read-from-clipboard write-to-clipboard-and-show-toast]]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn conversion-page
  []
  (let [initial-options {:pretty? true}
        options-ratom (r/atom initial-options)

        navigate-to-conversion-page
        (fn [input-type output-type]
          (rf/dispatch [:navigate-to-conversion-page {:handler :conversion
                                                      :route-params {:input-type input-type
                                                                     :output-type output-type}}]))
        update-input-text
        (fn [input-text]
          (rf/dispatch-sync [:update-input-text input-text @options-ratom]))

        swap-converter
        (fn []
          (rf/dispatch-sync [:swap-converter]))

        set-input-monaco-editor-instances
        (fn [input-editor input-monaco]
          (rf/dispatch [:set-input-monaco-editor-instances input-editor input-monaco]))

        set-output-monaco-editor-instances (fn [output-editor output-monaco]
                                             (rf/dispatch [:set-output-monaco-editor-instances output-editor output-monaco]))]
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
                                    (navigate-to-conversion-page input-type (first (keys (:convert-fns input-notation))))))}
            (->> notations
                 (filter (fn [{:keys [convert-fns]}] (pos? (count convert-fns))))
                 (map (fn [{:keys [notation-type label]}] ^{:key notation-type} [:option {:value notation-type} label])))]
           [:span "to"]
           [:select {:class "select select-ghost-primary w-44"
                     :value output-type
                     :on-change (fn [e]
                                  (let [output-type (keyword (.. e -target -value))]
                                    (navigate-to-conversion-page input-type output-type)))}
            (let [{:keys [convert-fns]} (notations-by-notation-type (keyword input-type))]
              (->> notations
                   (filter (fn [{:keys [notation-type]}] (notation-type convert-fns)))
                   (map (fn [{:keys [notation-type label]}] ^{:key notation-type} [:option {:value notation-type} label]))))]
           (let [output-notation (output-type notations-by-notation-type)]
             (when (get-in output-notation [:convert-fns input-type])
               [:span {:class "tooltip tooltip-top" :data-tooltip "Swap 'from' and 'to'"}
                [:button {:class "btn btn-sm"
                          :on-click (fn [] (swap-converter))}
                 [:> icons-go/GoArrowSwitch {:size "1.0em"}]]]))]]

         [:section {:class "grid grid-flow-col justify-stretch gap-4 "}
          [:div {:class "max-w-full"}
           [:div {:class "flex items-center my-2"}
            [:div {:class "ml-1"}
             "Input"]
            [:div {:class "flex justify-end gap-2 w-full mr-1"}
             [:button {:class "btn btn-sm"
                       :on-click #(update-input-text "")}
              [:> icons-lu/LuEraser {:size "1.3em"}]]
             (paste-button (fn [text] (update-input-text text)))]]
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
                        :on-mount (fn [editor monaco]
                                    (.setTheme (.-editor ^js monaco) editor-theme)
                                    (.setModelLanguage (.-editor ^js monaco) (.getModel ^js editor) (:editor-lang input-notation))
                                    (set-input-monaco-editor-instances editor monaco))}]]]
          [:div {:class "max-w-full"}
           [:div {:class "flex items-center my-2"}
            [:div {:class "flex gap-2 w-full"}
             (copy-button output-text)
             [:span
              {:class "tooltip tooltip-top" :data-tooltip "Paste the text on the clipboard to overwrite it with the conversion result."}
              ;; Paste And Copy Button
              [:button {:class "btn btn-sm"
                        :on-click (fn [_e]
                                    (read-from-clipboard
                                     (fn [text] (update-input-text text)
                                       ;;  TODO: Super hacky redundant `convert` execution
                                       (let [another-output-text (convert text {:input-type input-type :output-type output-type})
                                             toast-msg "Pasted and copied converted text 👍 "]
                                         (write-to-clipboard-and-show-toast another-output-text nil {:text toast-msg})))))}
               [:> icons-lia/LiaPasteSolid {:size "1.3em" :class "mr-1"}]
               "&"
               [:> icons-lia/LiaCopySolid {:size "1.3em" :class "ml-1"}]]]]
            [:div {:class "flex items-center gap-2 justify-end w-full"}
             [:label {:for "pretty-checkbox"}
              "pretty"]
             [:input {:class "switch switch-bordered-primary mr-2"
                      :type "checkbox"
                      :id "pretty-checkbox"
                      :checked (:pretty? @options-ratom)
                      :on-change (fn [_e] (swap! options-ratom update :pretty? not) (update-input-text input-text))}]]]

           [:div {:class "max-w-full"}
            [:> Editor {:height "70vh"
                        :width "99%"
                        :options {:minimap {:enabled false}
                                  :wordWrap (word-wrap-val editor-word-wrap)
                                  :read-only true}
                        :value (str output-text)
                        :on-mount (fn [editor monaco]
                                    (.setModelLanguage (.-editor ^js monaco) (.getModel ^js editor) (:editor-lang output-notation))
                                    (set-output-monaco-editor-instances editor monaco))}]]]]
         [:section
          [:span]]]))))
