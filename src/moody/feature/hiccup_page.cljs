(ns moody.feature.hiccup-page
  (:require
   ["@monaco-editor/react" :refer [Editor]]
   [moody.db.const :refer [input-data-types]]
   [re-frame.core :as rf]))

(defn hiccup-page
  []
  ;; [:f>]
  (let [;; options-ratom (r/atom {:input-data-type "jsx"
        ;;                        :pretty? true})
        set-options (fn [options]
                      (rf/dispatch [:set-options options]))
        convert (fn [input-text]
                  (rf/dispatch [:convert input-text]))]
    (fn []
      (let [options @(rf/subscribe [:options])
            input-text @(rf/subscribe [:input-text])
            output-text @(rf/subscribe [:output-text])]
        [:article
         [:h2 {:class "text-content1 text-lg mt-4"} "HTML - Hiccup"]
         [:section {:class "grid grid-flow-col justify-stretch gap-4 py-2"}
          [:div {:class "max-w-full"}
           [:div {:class "my-4"}
            "Input"
            [:select {:class "select select-ghost-secondary mx-4"
                      :value (:input-data-type options)
                      :on-change (fn [e]
                                   ;; (swap! options-ratom assoc :input-data-type (.. e -target -value))
                                   (set-options (assoc options :input-data-type (.. e -target -value)))
                                   (convert input-text))}
             (map (fn [{:keys [value label]}] ^{:key value} [:option {:value value} label]) input-data-types)]]
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
