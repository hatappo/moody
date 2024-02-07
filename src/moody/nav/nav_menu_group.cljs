(ns moody.nav.nav-menu-group
  (:require
   [moody.components.icons :refer [menu-icon]]
   [moody.router :as router]))

(defn nav-menu-group
  [id label tools icon]
  (let [html-id (str "menu-" (name id))]
    [:li
     [:input {:type "checkbox" :id html-id :class "menu-toggle" :default-checked false}]
     [:label {:class "menu-item justify-between" :for html-id}
      [:div {:class "flex gap-2"}
       [:> icon {:size "1.5em"}]
       [:span label]]
      [:<> menu-icon]]
     [:div {:class "menu-item-collapse"}
      [:div {:class "min-h-0"}
       (map (fn [{:keys [tool-type input-type output-type title]}]
              ^{:key tool-type}
              [:a {:href (router/path-for :conversion :input-type input-type :output-type output-type)}
               [:label {:class "menu-item ml-6"} title]])
            tools)]]]))
