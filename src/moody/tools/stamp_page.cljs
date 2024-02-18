(ns moody.tools.stamp-page
  (:require
   ["react-icons/lia" :as icons-lia]
   [clojure.string :as str]
   [moody.components.button :refer [copy-button]]
   [reagent.core :as r]))

(defn stamp-page
  []
  (let [canvas-black-atom (atom nil)
        canvas-white-atom (atom nil)
        color-ratom (r/atom "#404040")
        text-ratom (r/atom "Hello, World!")
        update-canvas (fn []
                        (doseq [canvas [@canvas-black-atom @canvas-white-atom]]
                          (let [ctx (.getContext canvas "2d")]
                            (.beginPath ctx)
                            (set! (.-width canvas) 128)
                            (set! (.-height canvas) 128)
                            (set! (.-font ctx) "16px serif")
                            (set! (.-fillStyle ctx) @color-ratom)
                            (set! (.-textBaseline ctx) "center")
                            (set! (.-textAlign ctx) "center")
                            (.fillText ctx @text-ratom 64 64))))]
    (fn []
      [:article {:class "flex flex-col gap-2"}
       [:h2 {:class "text-xl my-4"}
        "Stamp Emoji Generator"]

       [:div {:class "flex flex-wrap gap-20"}

        [:section {:class "flex flex-col gap-4"}
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
                                (update-canvas))}]]]

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

         [:div {:class "flex flex-col gap-1"}
          [:div {:class "flex gap-4"}
           [:div
            "Black BG:"
            [:canvas {:class "w-32 h-32 bg-black"
                      :ref (fn [canvas]
                             (reset! canvas-black-atom canvas))}]]
           [:div
            "White BG:"
            [:canvas {:class "w-32 h-32 bg-white"
                      :ref (fn [canvas]
                             (reset! canvas-white-atom canvas))}]]]]


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
