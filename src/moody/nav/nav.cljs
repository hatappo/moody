(ns moody.nav.nav
  (:require
   ["react-icons/go" :as icons-go]
   ["react-icons/io5" :as icons-io5]
   ["react-icons/lu" :as icons-lu]
   ["react-icons/si" :as icons-si]
   ["react-icons/tb" :as icons-tb]
   ["react-icons/ti" :as icons-ti]
   ["react-icons/vsc" :as icons-vsc]
   [moody.config.env :as env]
   [moody.nav.nav-menu-group :refer [nav-menu-group]]
   [moody.router :as router]
   [moody.tools.tools :as tools]
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
             (nav-menu-group :html "HTML" tools/html-tools icons-ti/TiHtml5)
             (nav-menu-group :clojure "Clojure" tools/clojure-tools icons-si/SiClojure)
             (nav-menu-group :encoding "Encoder / Decoder" tools/encoding-tools icons-lu/LuBinary)
             (nav-menu-group :radix "Radix" tools/radix-tools icons-tb/TbNumber16Small)
             (nav-menu-group :table "Table" tools/table-tools icons-go/GoTable)]]

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
