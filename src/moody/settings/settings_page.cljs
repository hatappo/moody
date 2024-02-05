(ns moody.settings.settings-page
  (:require
   [moody.tools.editor :refer [themes whitespace-options]]
   [re-frame.core :as rf]))

(defn settings-page
  []
  (let [set-editor-theme (fn [editor-theme]
                           (rf/dispatch [:set-editor-theme editor-theme]))
        set-editor-word-wrap (fn [word-wrap]
                               (rf/dispatch [:set-editor-word-wrap word-wrap]))
        set-editor-whitespace (fn [whitespace-option]
                                (rf/dispatch [:set-editor-whitespace-option whitespace-option]))]
    (fn []
      (let [editor-theme @(rf/subscribe [:editor-theme])
            editor-word-wrap @(rf/subscribe [:editor-word-wrap])
            editor-whitespace-option @(rf/subscribe [:editor-whitespace-option])]
        [:article {:class "flex flex-col gap-4"}
         [:h2 {:class "text-xl my-8"}
          "Settings"]
         [:section {:class "flex flex-col gap-4"}
          [:h3
           "Text Editor"]
          [:div {:class "flex items-center gap-2"}
           [:label {:for "theme-select"}
            "Theme"]
           [:select {:class "select select-ghost-primary"
                     :id "theme-select"
                     :value editor-theme
                     :on-change (fn [e]
                                  (let [theme (.. e -target -value)]
                                    (set-editor-theme theme)))}
            (map (fn [{:keys [val]}] ^{:key val} [:option {:value val} val]) themes)]]
          [:div {:class "flex items-center gap-2"}
           [:label {:for "word-wrap-checkbox"}
            "Word wrap"]
           [:input {:class "switch switch-bordered-primary mr-2"
                    :type "checkbox"
                    :id "word-wrap-checkbox"
                    :checked editor-word-wrap
                    :on-change (fn [_e] (set-editor-word-wrap (not editor-word-wrap)))}]]
          [:div {:class "flex items-center gap-2"}
           [:label {:for "whitespace-select"}
            "Render whitespace"]
           [:select {:class "select select-ghost-primary"
                     :id "whitespace-select"
                     :value editor-whitespace-option
                     :on-change (fn [e]
                                  (let [whitespace-option (.. e -target -value)]
                                    (set-editor-whitespace whitespace-option)))}
            (map (fn [opt] ^{:key opt} [:option {:value opt} opt]) whitespace-options)]]]]))))
