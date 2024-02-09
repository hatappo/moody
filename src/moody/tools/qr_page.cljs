(ns moody.tools.qr-page
  (:require
   ["qrcode" :as QRCode]
   [moody.components.button :refer [copy-button paste-button]]
   [moody.util-str :refer [pad]]
   [reagent.core :as r]
   [taoensso.timbre :as timbre]))

(def error-correction-levels
  [{:val "L" :error-resistance-percent 7 :level "Low"}
   {:val "M" :error-resistance-percent 15 :level "Medium"}
   {:val "Q" :error-resistance-percent 25 :level "Quartile"}
   {:val "H" :error-resistance-percent 30 :level "High"}])

(def image-types ["image/svg+xml" "image/png" "image/jpeg" "image/webp"])

(def mask-patterns
  [{:val "" :label "Auto"}
   {:val 0 :label 0}
   {:val 1 :label 1}
   {:val 2 :label 2}
   {:val 3 :label 3}
   {:val 4 :label 4}
   {:val 5 :label 5}
   {:val 6 :label 6}
   {:val 7 :label 7}])

(def versions
  (cons
   {:val "" :label "Auto"}
   (map #(array-map :val % :label %) (range 1 41))))

(def opacities
  (concat [{:val "00" :label "00 (transparent)"}]
          (map (fn [n]
                 (let [hex (pad (.toString n 16) 2 "0")]
                   (array-map :val hex :label hex)))
               (range 1 255))
          [{:val "ff" :label "ff (opaque)"}]))

(defn to-hex [n] (-> n str (js/Number.parseInt) (.toString 16) (pad 2 "0")))

(defn qr-page
  []
  (let [level-ratom (r/atom "M")
        type-ratom (r/atom "image/svg+xml")
        version-ratom (r/atom "")
        scale-ratom (r/atom 4)
        margin-ratom (r/atom 4)
        mask-pattern-ratom (r/atom "")
        color-dark-ratom (r/atom "#000000")
        color-light-ratom (r/atom "#ffffff")
        opacity-dark-ratom (r/atom "255")
        opacity-light-ratom (r/atom "255")
        bg-pattern-ratom (r/atom false)
        text-ratom (r/atom "https://example.com/")
        data-url-ratom (r/atom nil)
        data-text-ratom (r/atom nil)
        error-ratom (r/atom nil)
        set-data-url #(if (empty? @text-ratom)
                        (do (reset! error-ratom nil)
                            (reset! data-url-ratom nil))
                        (let [options (clj->js
                                       {:errorCorrectionLevel @level-ratom
                                        :type @type-ratom
                                        :version @version-ratom
                                        :scale @scale-ratom
                                        :margin @margin-ratom
                                        :maskPattern @mask-pattern-ratom
                                        :color {:dark (str @color-dark-ratom (to-hex @opacity-dark-ratom))
                                                :light (str @color-light-ratom (to-hex @opacity-light-ratom))}})]
                          (timbre/info options)
                          (.toDataURL QRCode
                                      @text-ratom
                                      options
                                      (fn [err url]
                                        (if err
                                          (do (reset! error-ratom (str err))
                                              (reset! data-url-ratom nil)
                                              (reset! data-text-ratom nil))
                                          (do (reset! error-ratom nil)
                                              (reset! data-url-ratom url)
                                              (reset! data-text-ratom nil)))))
                          (when (= @type-ratom "image/svg+xml")
                            (.toString QRCode
                                       @text-ratom
                                       options
                                       (fn [err text]
                                         (if err
                                           (reset! data-text-ratom nil)
                                           (reset! data-text-ratom text)))))))]

    (set-data-url)

    (fn []
      [:article {:class "flex flex-col gap-2"}
       [:h2 {:class "text-xl my-4"}
        "QR Code Generator"]

       [:div {:class "flex flex-wrap gap-20"}

        [:section {:class "flex flex-col gap-4"}

         [:div {:class "flex items-center"}
          [:label {:class "w-32"
                   :for "error-correction-level-select"}
           "Error correction:"]
          [:select {:class "select select-ghost-primary w-64"
                    :id "error-correction-level-select"
                    :value @level-ratom
                    :on-change (fn [e]
                                 (reset! level-ratom (.. e -target -value))
                                 (set-data-url))}
           (map (fn [{:keys [val level error-resistance-percent]}]
                  ^{:key val} [:option {:value val} (str level " (~" error-resistance-percent "%)")])
                error-correction-levels)]]

         [:div {:class "flex items-center"}
          [:label {:class "w-32"
                   :for "image-type-select"}
           "Image type:"]
          [:select {:class "select select-ghost-primary w-64"
                    :id "image-type-select"
                    :value @type-ratom
                    :on-change (fn [e]
                                 (reset! type-ratom (.. e -target -value))
                                 (set-data-url))}
           (map (fn [type]
                  ^{:key type} [:option {:value type} type])
                image-types)]]

         [:div {:class "flex items-center"}
          [:label {:class "w-32"
                   :for "version-input"}
           "Version:"]
          [:select {:class "select select-ghost-primary w-64"
                    :id "version-select"
                    :value @version-ratom
                    :on-change (fn [e]
                                 (reset! version-ratom (.. e -target -value))
                                 (set-data-url))}
           (map (fn [{:keys [val label]}]
                  ^{:key label} [:option {:value val} label])
                versions)]]

         [:div {:class "flex items-center"}
          [:label {:class "w-32"
                   :for "scale-input"}
           "Scale: " @scale-ratom]
          [:input {:class "range w-64"
                   :type "range"
                   :min 1
                   :max 64
                   :id "scale-input"
                   :value @scale-ratom
                   :on-change (fn [e]
                                (reset! scale-ratom (.. e -target -value))
                                (set-data-url))}]]

         [:div {:class "flex items-center"}
          [:label {:class "w-32"
                   :for "margin-input"}
           "Margin: " @margin-ratom]
          [:input {:class "range w-64"
                   :type "range"
                   :min 0
                   :max 8
                   :id "margin-input"
                   :value @margin-ratom
                   :on-change (fn [e]
                                (reset! margin-ratom (.. e -target -value))
                                (set-data-url))}]]

         [:div {:class "flex items-center"}
          [:label {:class "w-32"
                   :for "mask-pattern-select"}
           "Mask Pattern:"]
          [:select {:class "select select-ghost-primary w-64"
                    :id "mask-pattern-select"
                    :value @mask-pattern-ratom
                    :on-change (fn [e]
                                 (reset! mask-pattern-ratom (.. e -target -value))
                                 (set-data-url))}
           (map (fn [{:keys [val label]}]
                  ^{:key label} [:option {:value val} label])
                mask-patterns)]]

         [:div
          "Dark color"
          [:div {:class "flex flex-col gap-2 py-2 px-4"}
           [:div {:class "flex items-center gap-2"}
            [:label {:for "color-dark-input"}
             "Color code:"]
            [:span @color-dark-ratom]
            [:input {:class "input input-ghost-primary w-16"
                     :type "color"
                     :id "color-dark-input"
                     :value @color-dark-ratom
                     :on-change (fn [e]
                                  (reset! color-dark-ratom (.. e -target -value))
                                  (set-data-url))}]]
           [:div {:class "flex items-center justify-between gap-2"}
            [:label {:for "opacity-dark-input"}
             "Opacity:"]
            [:span (to-hex @opacity-dark-ratom)]
            [:input {:class "range w-64"
                     :type "range"
                     :min 0
                     :max 255
                     :id "opacity-dark-input"
                     :value @opacity-dark-ratom
                     :on-change (fn [e]
                                  (reset! opacity-dark-ratom (.. e -target -value))
                                  (set-data-url))}]]]]

         [:div
          "Light color"
          [:div {:class "flex flex-col gap-2 py-2 px-4"}
           [:div {:class "flex items-center gap-2"}
            [:label {:for "color-light-input"}
             "Color code:"]
            [:span @color-light-ratom]
            [:input {:class "input input-ghost-primary w-16"
                     :type "color"
                     :id "color-light-input"
                     :value @color-light-ratom
                     :on-change (fn [e]
                                  (reset! color-light-ratom (.. e -target -value))
                                  (set-data-url))}]]
           [:div {:class "flex items-center justify-between gap-2"}
            [:label {:for "opacity-light-input"}
             "Opacity:"]
            [:span (to-hex @opacity-light-ratom)]
            [:input {:class "range w-64"
                     :type "range"
                     :min 0
                     :max 255
                     :id "opacity-light-input"
                     :value @opacity-light-ratom
                     :on-change (fn [e]
                                  (reset! opacity-light-ratom (.. e -target -value))
                                  (set-data-url))}]]]]

         [:div {:class "flex items-center"}
          [:label {:class "mr-2"
                   :for "bg-pattern-checkbox"}
           "Display background checker pattern:"]
          [:input {:class "switch switch-ghost-primary"
                   :type "checkbox"
                   :checked @bg-pattern-ratom
                   :on-change #(swap! bg-pattern-ratom not)
                   :id "bg-pattern-checkbox"}]]]


        [:section {:class "flex flex-col gap-8 flex-1 min-w-96 mr-8"}

         [:div {:class "flex flex-col gap-1 max-w-xl"}
          [:div {:class "flex items-center gap-1 "}
           [:span {:class "mr-auto"}
            "Text:"]
           [copy-button (fn [] @text-ratom)]
           [paste-button (fn [text] (reset! text-ratom text))]]
          [:textarea {:class "textarea textarea-ghost-primary max-w-xl"
                      :rows 8
                      :value @text-ratom
                      :on-change (fn [e]
                                   (reset! text-ratom (.. e -target -value))
                                   (set-data-url))}]]

         [:div {:class "flex flex-col gap-1"}
          "QR Code:"
          [:span {:class "text-error"}
           @error-ratom]
          [:div (when @bg-pattern-ratom {:class "moody-bg-transparent-checker w-fit p-5"})
           [:img {:src @data-url-ratom}]
           #_[:span {:dangerouslySetInnerHTML {:__html @data-text-ratom}}]]]
         (when @data-text-ratom
           [:div {:class "flex flex-col gap-1 max-w-xl"}
            [:div {:class "flex items-center"}
             [:span {:class "mr-auto"}
              "svg:"]
             [copy-button (fn [] @data-text-ratom)]]
            [:textarea {:class "textarea textarea-solid max-w-xl"
                        :rows 20
                        :read-only true
                        :on-click #(.select (.. % -target))
                        :value @data-text-ratom}]])]]])))
