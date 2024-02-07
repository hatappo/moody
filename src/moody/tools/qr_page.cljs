(ns moody.tools.qr-page
  (:require
   ["qrcode" :as QRCode]
   [reagent.core :as r]))

(def error-correction-levels
  [{:val "L" :error-resistance-percent 7 :level "Low"}
   {:val "M" :error-resistance-percent 15 :level "Medium"}
   {:val "Q" :error-resistance-percent 25 :level "Quartile"}
   {:val "H" :error-resistance-percent 30 :level "High"}])

(def image-types ["image/png" "image/jpeg" "image/webp"])

(def mask-patterns
  [{:val nil :label "Auto"}
   {:val 0 :label 0}
   {:val 1 :label 1}
   {:val 2 :label 2}
   {:val 3 :label 3}
   {:val 4 :label 4}
   {:val 5 :label 5}
   {:val 6 :label 6}
   {:val 7 :label 7}])

(defn qr-page
  []
  (let [level-ratom (r/atom "M")
        type-ratom (r/atom "image/png")
        version-ratom (r/atom 1)
        scale-ratom (r/atom 4)
        margin-ratom (r/atom 4)
        mask-pattern-ratom (r/atom nil)
        color-dark-ratom (r/atom "#000000ff")
        color-light-ratom (r/atom "#ffffffff")
        text-ratom (r/atom "abcde")
        data-url-ratom (r/atom nil)
        set-data-url #(.toDataURL QRCode
                                  @text-ratom
                                  (clj->js {:errorCorrectionLevel @level-ratom
                                            :type @type-ratom
                                            :version @version-ratom
                                            :scale @scale-ratom
                                            :margin @margin-ratom
                                            :maskPattern @mask-pattern-ratom
                                            :color {:dark @color-dark-ratom
                                                    :light @color-light-ratom}})
                                  (fn [_err url] (reset! data-url-ratom url)))]

    (set-data-url)

    (fn []
      [:article {:class "flex flex-col gap-2"}
       [:h2 {:class "text-xl my-4"}
        "QR Code Generator"]

       [:section {:class "flex flex-col gap-4 pr-8"}

        [:div {:class "flex items-center gap-1"}
         [:label {:class "w-32"
                  :for "error-correction-level-select"}
          "Error correction:"]
         [:select {:class "select select-ghost-primary"
                   :id "error-correction-level-select"
                   :value @level-ratom
                   :on-change (fn [e]
                                (reset! level-ratom (.. e -target -value))
                                (set-data-url))}
          (map (fn [{:keys [val level error-resistance-percent]}]
                 ^{:key val} [:option {:value val} (str level " (~" error-resistance-percent "%)")])
               error-correction-levels)]]

        [:div {:class "flex items-center gap-1"}
         [:label {:class "w-32"
                  :for "image-type-select"}
          "Image type:"]
         [:select {:class "select select-ghost-primary"
                   :id "image-type-select"
                   :value @type-ratom
                   :on-change (fn [e]
                                (reset! type-ratom (.. e -target -value))
                                (set-data-url))}
          (map (fn [type]
                 ^{:key type} [:option {:value type} type])
               image-types)]]

        [:div {:class "flex items-center gap-1"}
         [:label {:class "w-32"
                  :for "version-input"}
          "Version:"]
         [:input {:class "select select-ghost-primary"
                  :type "number"
                  :min 1
                  :max 40
                  :id "version-input"
                  :value @version-ratom
                  :on-change (fn [e]
                               (reset! version-ratom (.. e -target -value))
                               (set-data-url))}]]

        [:div {:class "flex items-center gap-1"}
         [:label {:class "w-32"
                  :for "scale-input"}
          "Scale:"]
         [:input {:class "select select-ghost-primary"
                  :type "number"
                  :min 1
                  :max 50
                  :id "scale-input"
                  :value @scale-ratom
                  :on-change (fn [e]
                               (reset! scale-ratom (.. e -target -value))
                               (set-data-url))}]]

        [:div {:class "flex items-center gap-1"}
         [:label {:class "w-32"
                  :for "margin-input"}
          "Margin:"]
         [:input {:class "select select-ghost-primary"
                  :type "number"
                  :min 0
                  :max 10
                  :id "margin-input"
                  :value @margin-ratom
                  :on-change (fn [e]
                               (reset! margin-ratom (.. e -target -value))
                               (set-data-url))}]]

        [:div {:class "flex items-center gap-1"}
         [:label {:class "w-32"
                  :for "mask-pattern-select"}
          "Mask Pattern:"]
         [:select {:class "select select-ghost-primary"
                   :id "mask-pattern-select"
                   :value @mask-pattern-ratom
                   :on-change (fn [e]
                                (reset! mask-pattern-ratom (.. e -target -value))
                                (set-data-url))}
          (map (fn [{:keys [val label]}]
                 ^{:key label} [:option {:value val} label])
               mask-patterns)]]

        [:div {:class "flex items-center gap-1"}
         [:label {:class "w-32"
                  :for "color-dark-input"}
          "Dark color:"]
         [:input {:class "select select-ghost-primary"
                  :type "color"
                  :min 0
                  :max 10
                  :id "color-dark-input"
                  :value @color-dark-ratom
                  :on-change (fn [e]
                               (reset! color-dark-ratom (.. e -target -value))
                               (set-data-url))}]]

        [:div {:class "flex items-center gap-1"}
         [:label {:class "w-32"
                  :for "color-light-input"}
          "Light color:"]
         [:input {:class "select select-ghost-primary"
                  :type "color"
                  :min 0
                  :max 10
                  :id "color-light-input"
                  :value @color-light-ratom
                  :on-change (fn [e]
                               (reset! color-light-ratom (.. e -target -value))
                               (set-data-url))}]]

        [:div {:class "flex flex-col gap-1"}
         "Text"
         [:textarea {:class "textarea textarea-ghost-primary"
                     :rows 4
                     :value @text-ratom
                     :on-change (fn [e]
                                  (reset! text-ratom (.. e -target -value))
                                  (set-data-url))}]]

        [:div {:class "flex flex-col gap-1"}
         "QR Code"
         [:span
          [:img {:src @data-url-ratom}]]]]])))
