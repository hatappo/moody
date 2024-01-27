(ns moody.feature.hiccup-page
  (:require
   ["@monaco-editor/react" :refer [Editor]]
   [re-frame.core :as rf]))

(defn hiccup-page
  []
  ;; [:f>]
  (let [convert-html-to-hiccup (fn [input-text]
                                 (rf/dispatch [:convert-html-to-hiccup input-text {:pretty? true}]))]
    (fn []
      (let [input-text @(rf/subscribe [:input-text])
            output-text @(rf/subscribe [:output-text])]
        [:article
         [:h2 {:class "text-content1 text-lg mt-4"} "HTML - Hiccup"]
         [:section {:class "grid grid-flow-col justify-stretch gap-4 p-2"}
          [:div {:class "max-w-full"}
           "Input"
           [:div {:class "max-w-full my-1"}
            [:> Editor
             {:class ""
              :height "75vh"
              :width "95%"
              :options {:minimap {:enabled false}
                        :wordWrap "on"}
              #_#_:theme "vs-dark"
              :value input-text
              :on-change convert-html-to-hiccup #_(fn [value] (js/console.log value))
              :defaultLanguage "html"
              #_#_:defaultValue "<a href=\"/#hoge\">\nあいうえお\n</a>"}]]]
          [:div {:class "max-w-full"}
           "Output"
           [:div {:class "max-w-full my-1"}
            [:> Editor
             {:class ""
              :height "75vh"
              :width "95%"
              :options {:minimap {:enabled false}
                        :wordWrap "on"
                        :read-only true}
              :value output-text
              :defaultLanguage "clojure"
              #_#_:defaultValue "[:a\n {:href \"/#hoge\"}\n \"あいうえお\"]"}]]]]]))))
