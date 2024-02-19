(ns moody.about.about-page
  (:require
   [moody.router :as router]))

(defn about-page
  []
  (fn []
    (let [_a nil]
      [:article {:class "flex flex-col gap-2"}
       [:h2 {:class "text-xl my-4"}
        "Moody Dev Tools"]
       [:section {:class "m-4"}
        [:p
         "This tools accepts and transforms various data structures from left to right."]
        [:p
         "If you like this tool, please star it on GitHub. If you have a feature you'd like to see, please create an issue."]
        [:a {:href (router/path-for :home)}
         [:button {:class "link link-underline my-8"} "Show All Tools"]]]])))
