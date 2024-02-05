(ns moody.home.views.home-page
  (:require
   [moody.router :as router]))

(defn home-page
  []
  [:f>
   (fn []
     (let [_a nil]
       [:article {:class "flex flex-col gap-2"}
        [:h2 {:class "text-xl my-4"}
         "Moody Dev Tools"]
        [:section {:class "m-4"}
         [:p "It accepts and transforms various data structures from left to right."]
         [:a {:href (router/path-for :cards)}
          [:button {:class "btn btn-solid-primary my-4"} "Show All Tools"]]]]))])
