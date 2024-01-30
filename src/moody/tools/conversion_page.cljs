(ns moody.tools.conversion-page
  (:require
   ["@monaco-editor/react" :refer [Editor]]
   [moody.db.const :refer [conversion-tools]]
   [re-frame.core :as rf]))

(defn hiccup-page
  []
  ;; [:f>]
  (let [set-tool-type (fn [tool-type]
                        (rf/dispatch [:set-tool-type tool-type]))
        convert (fn [input-text]
                  (rf/dispatch [:convert input-text]))]
    (fn []
      (let [{:keys [tool-type]} @(rf/subscribe [:nav])
            input-text @(rf/subscribe [:input-text])
            output-text @(rf/subscribe [:output-text])
            #_#_options @(rf/subscribe [:options])]
        [:article
         [:h2 {:class "text-content1 text-lg my-4"} "Data Format Conversion"]
         [:select {:class "select select-ghost-primary"
                   :value tool-type
                   :on-change (fn [e]
                                (set-tool-type (.. e -target -value))
                                (convert input-text))}
          (map (fn [{:keys [tool-type label]}] ^{:key tool-type} [:option {:value tool-type} label]) conversion-tools)]
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
