(ns moody.tools.stamp-page
  (:require
   ["react-icons/lia" :as icons-lia]
   [clojure.string :as str]
   [moody.components.button :refer [copy-button]]
   [reagent.core :as r]))

(def font-families
  {"serif" "Serif"
   "roboto" "Roboto"})

(def colors
  {:Gray "#808080"
   :DodgerBlue "#1E90FF"
   :DarkTurquoise "#00CED1"
   :Gold "#FFD700"
   :Orange "#FFA500"
   :HotPink "#FF69B4"
   :Fuchsia "#FF00FF"})

(def initial-values
  {:width 128
   :height 128
   :fixed-ratio true
   :color (:Gray colors)
   :font-family (get font-families "serif")
   :text "Yes\nNo!"
   :canvas-black nil
   :canvas-white nil})

(defn stamp-page
  []
  (let [width-ratom (r/atom (:width initial-values))
        height-ratom (r/atom (:height initial-values))
        fixed-ratio-ratom (r/atom (:fixed-ratio initial-values))
        color-ratom (r/atom (:color initial-values))
        font-family-ratom (r/atom (:font-family initial-values))
        text-ratom (r/atom (:text initial-values))
        canvas-black-atom (atom (:canvas-black initial-values))
        canvas-white-atom (atom (:canvas-white initial-values))
        update-canvas (fn []
                        (doseq [canvas [@canvas-black-atom @canvas-white-atom]]
                          (when canvas
                            (let [ctx (.getContext canvas "2d")
                                  width @width-ratom
                                  height @height-ratom
                                  lines (str/split (str/trim @text-ratom) "\n")
                                  cnt (count lines)
                                  line-height (int (/ height cnt))]
                              (.beginPath ctx)
                              (set! (.-width canvas) width)
                              (set! (.-height canvas) height)
                              (set! (.-font ctx) (str " " line-height "px " @font-family-ratom))
                              (set! (.-fillStyle ctx) @color-ratom)
                              (set! (.-textBaseline ctx) "top")
                              (set! (.-textAlign ctx) "center")
                              (doseq [[idx line] (map-indexed vector lines)]
                                (.fillText ctx line (/ width 2) (* line-height idx) width))))))]
    (fn []
      [:article {:class "flex flex-col gap-2"}
       [:h2 {:class "text-xl my-4"}
        "Stamp Emoji Generator"]

       [:div {:class "flex flex-wrap gap-20"}

        [:section {:class "flex flex-col gap-4"}

         [:div {:class "flex items-center"}
          [:label {:class "w-32"
                   :for "width-input"}
           "Width: " @width-ratom]
          [:input {:class "range w-64"
                   :type "range"
                   :min 16
                   :max 256
                   :id "width-input"
                   :value @width-ratom
                   :on-change (fn [e]
                                (reset! width-ratom (.. e -target -value))
                                (when @fixed-ratio-ratom (reset! height-ratom (.. e -target -value)))
                                (update-canvas))}]]

         [:div {:class "flex items-center"}
          [:label {:class "w-32"
                   :for "height-input"}
           "Height: " @height-ratom]
          [:input {:class "range w-64"
                   :type "range"
                   :min 16
                   :max 256
                   :id "height-input"
                   :value @height-ratom
                   :on-change (fn [e]
                                (reset! height-ratom (.. e -target -value))
                                (when @fixed-ratio-ratom (reset! width-ratom (.. e -target -value)))
                                (update-canvas))}]]

         [:div {:class "flex items-center"}
          [:label {:class "mr-2"
                   :for "fixed-ratio-checkbox"}
           "Fixed ratio:"]
          [:input {:class "switch switch-ghost-primary"
                   :type "checkbox"
                   :checked @fixed-ratio-ratom
                   :on-change #(swap! fixed-ratio-ratom not)
                   :id "fixed-ratio-checkbox"}]]

         [:div {:class "flex items-center gap-2"}
          [:label {:for "color-input"}
           "Color code:"]
          [:span @color-ratom]
          [:input {:class "input input-ghost-primary w-16"
                   :type "color"
                   :id "color-input"
                   :value @color-ratom
                   :on-change (fn [e]
                                (reset! color-ratom (.. e -target -value))
                                (update-canvas))}]]

         [:div {:class "flex items-center gap-4"}
          (map (fn [[name code]]
                 ^{:key name}
                 [:span {:class "tooltip tooltip-bottom" :data-tooltip name}
                  [:button {:class "w-4 h-4 cursor-pointer"
                            :style {:background code}
                            :on-click #(reset! color-ratom code)}]])
               colors)]

         [:div {:class "flex items-center gap-2"}
          [:label {:for "font-family-select"}
           "Font:"]
          [:select {:class "select select-ghost-primary"
                    :id "font-family-select"
                    :on-change (fn [e]
                                 (reset! font-family-ratom (.. e -target -value))
                                 (update-canvas))}
           (map (fn [[val label]]
                  ^{:key val}
                  [:option {:value val}
                   label])
                font-families)]]]

        [:section {:class "flex flex-col gap-8 mr-8"}

         [:div {:class "flex flex-col gap-1"}
          [:div {:class "flex items-center justify-between gap-1 "}
           [:span {:class ""}
            "Text:"]
           (copy-button @text-ratom)
           ;;  TODO: not working well with `:default-value`
           #_(paste-button (fn [text] (reset! text-ratom text)))]
          [:textarea {:class "textarea textarea-ghost-primary"
                      :default-value @text-ratom
                      :on-change (fn [e]
                                   (reset! text-ratom (.. e -target -value))
                                   (update-canvas))
                      :rows 4}]]

         [:div {:class "flex gap-4"}
          [:div
           "Black BG:"
           [:canvas {:class "bg-black"
                     :ref (fn [canvas]
                            (when canvas
                              (reset! canvas-black-atom canvas)))}]]
          [:div
           "White BG:"
           [:canvas {:class "bg-white"
                     :ref (fn [canvas]
                            (when canvas
                              (reset! canvas-white-atom canvas)
                              (update-canvas)))}]]]

         [:div
          [:span {:class "tooltip tooltip-bottom" :data-tooltip "as a transparent png file"}
           [:button {:class "btn btn-sm btn-outline-primary gap-2"
                     :on-click (fn [_e]
                                 (let [a (.createElement js/document "a")
                                       data-url (.toDataURL @canvas-black-atom "image/png")]
                                   (set! (.-href a) data-url)
                                   (set! (.-download a) (str (str/replace @text-ratom "\n" "_")))
                                   (.click a)))}
            [:> icons-lia/LiaDownloadSolid {:size "1.3em"}]
            "Download"]]]]]])))
