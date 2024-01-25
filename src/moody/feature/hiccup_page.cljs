(ns moody.feature.hiccup-page)

(defn hiccup-page
  []
  ;; [:f>]
  (fn []
    (let [_a nil]
      [:article
       [:h2 {:class "text-content1 text-lg my-4"} "HTML - Hiccup"]
       [:div "Hiccup, Hiccup, Hiccup, Hiccup, "]])))
