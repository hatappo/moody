(ns moody.nav.nav
  (:require
   #_["react-icons/io5" :as icons-io5]
   ["react-icons/si" :as icons-si]
   ["react-icons/vsc" :as icons-vsc]
   [moody.components.icons :refer [menu-icon]]
   [moody.config.env :as env]
   [moody.router :as router]
   [moody.tools.tools :as const]
   [reagent.core :as r]))

(defn nav
  []
  (let [img-file-ratom (r/atom (rand-nth ["moody1-icon.jpg" "moody2-icon.jpg"]))]
    (fn []
      (let [_a nil]
        [:aside {:class "sidebar sidebar-fixed-left sidebar-mobile h-full justify-start max-sm:fixed max-sm:-translate-x-full"}
         [:section
          [:a {:class "sidebar-title items-center p-4" :href (router/path-for :home)}
           [:img {:src (str "/img/" @img-file-ratom) :class "m-3 w-7 h-7 rounded-md"}]
           [:div {:class "flex flex-col"}
            [:span "Moody"]
            [:span {:class "text-xs font-normal text-content2"} "Dev Tools"]]
           (when (:dev? (env/config))
             [:span {:class "badge badge-outline-secondary mx-4"} "Dev env"])]]

         [:section {:class "sidebar-content"}
          [:nav {:class "menu rounded-md"}

           ;; Menu
           [:section {:class "menu-section px-4"}
            [:span {:class "menu-title"} "Menu"]
            [:ul {:class "menu-items"}
             [:li
              [:input {:type "checkbox" :id "menu-all" :class "menu-toggle"}]
              [:a {:href (router/path-for :cards)}
               [:label {:class "menu-item justify-between" :for "menu-all"}
                [:div {:class "flex gap-2"}
                 [:> icons-vsc/VscHome {:size "1.5em"}]
                 [:span "All Tools"]]]]]
             [:li
              [:input {:type "checkbox" :id "menu-clojure" :class "menu-toggle"}]
              [:label {:class "menu-item justify-between" :for "menu-clojure"}
               [:div {:class "flex gap-2"}
                [:> icons-si/SiClojure {:size "1.5em"}]
                [:span "Clojure"]]
               [:<> menu-icon]]
              [:div {:class "menu-item-collapse"}
               [:div {:class "min-h-0"}
                (map (fn [{:keys [tool-type tool-category title]}]
                       ^{:key tool-type}
                       [:a {:href (router/path-for tool-category :tool-type tool-type)}
                        [:label {:class "menu-item ml-6"} title]])
                     const/clojure-tools)]]]]]

           [:div {:class "divider my-0"}]

           ;; Settings 
           [:section {:class "menu-section px-4"}
            #_[:span {:class "menu-title"} "Settings"]
            [:ul {:class "menu-items"}
             [:li
              [:a {:href (router/path-for :settings)}
               [:div {:class "menu-item"}
                [:> icons-vsc/VscGear {:size "1.5em"}]
                [:span "Settings"]]]]
             [:li
              [:a {:href "https://github.com/hatappo/moody" :target "_blank"}
               [:div {:class "menu-item"}
                [:> icons-vsc/VscGithub {:size "1.5em"}]
                #_[:> icons-io5/IoOpenOutline {:size "1.5em"}]]]]]]]]]))))
