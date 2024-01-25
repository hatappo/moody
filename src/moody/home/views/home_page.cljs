(ns moody.home.views.home-page
  (:require
   [moody.router :as router]))

(defn home-page
  []
  [:f>
   (fn []
     (let [_a nil]
       [:article {:class "m-4"}

        [:button {:class "btn btn-primary"} "Hello World!"]


        [:section {:class "m-20"}
         "Home"
         [:ul {:class "list-disc text-sm"}
          [:li {:class "m-2 p-2 flex flex-row align-middle"}
           [:a {:class "link" :href (router/path-for :home)}
            "aaaa"]]]
         [:ul {:class "list-disc text-sm"}
          [:li {:class "m-2 p-2 flex flex-row align-middle"}
           [:a {:class "link" :href (router/path-for :home)}
            "bbbb"]]]]]))])
