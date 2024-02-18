(ns moody.tools.filetype-page
  (:require
   ["filepond-plugin-image-preview" :as FilePondPluginImagePreview]
   ["magika" :refer [Magika]]
   ["react-filepond" :refer [FilePond registerPlugin]]
   [reagent.core :as r]
   [taoensso.timbre :as timbre]))

(def initial-values
  {:prediction {"label" "❓" "score" "❓"}})

(defn filetype-page
  []
  (let [prediction-ratom (r/atom (:prediction initial-values))]
    (fn []
      (let [_nil nil]
        [:article {:class "flex flex-col gap-2"}
         [:h2 {:class "text-xl my-4"}
          "Filetype Detection"]

         [:section {:class "flex flex-col gap-6 pr-8"}

          [:div {:class "flex flex-col gap-1"}
           [:link {:rel "stylesheet" :type "text/css" :href "/css/filepond.min.css"}]
           [:link {:rel "stylesheet" :type "text/css" :href "/css/filepond-plugin-image-preview.min.css"}]
           (registerPlugin FilePondPluginImagePreview)
           [:> FilePond  {:oninitfile #(timbre/info :oninitfile %)
                          :onaddfilestart #(timbre/info :onaddfilestart %)
                          :onaddfile (fn [err file]
                                       (timbre/info :onaddfile err file)
                                       (when-not err
                                         (let [reader (js/FileReader.)]
                                           (set! (.-onload reader)
                                                 (fn [e]
                                                   (let [array-buffer (.. e -target -result)
                                                         file-bytes (js/Uint8Array. array-buffer)
                                                         magika (Magika.)]
                                                     (-> (.load magika #js {})
                                                         (.then (fn []
                                                                  (-> (.identifyBytes magika file-bytes)
                                                                      (.then (fn [prediction]
                                                                               (reset! prediction-ratom (js->clj prediction)))))))))))
                                           (.readAsArrayBuffer reader (.-file file)))))
                          :onremovefile (fn [err file]
                                          (timbre/info :onremovefile err file)
                                          (reset! prediction-ratom (:prediction initial-values)))
                          :onerror #(timbre/warn :onerror %)
                          :maxFiles 1
                          :name "files"
                          :className "w-96"
                          ;; :credits {:label "" :url ""}
                          :stylePanelAspectRatio "0.25"
                          :labelIdle "Drag & Drop a file or <span class=\" filepond--label-action \">Browse</span>"}]]

          (let [{:strs [label score]} @prediction-ratom]
            [:div {:class "text-xl"}
             [:p
              "Filetype: "
              [:span {:class "text-primary"}
               label]]
             [:p
              "Probability: "
              [:span {:class "text-primary"}
               score]]])]]))))
