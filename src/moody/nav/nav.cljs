(ns moody.nav.nav
  (:require
   ["react-icons/bs" :as icons-bs]
   ["react-icons/go" :as icons-go]
   ["react-icons/io5" :as icons-io5]
   ["react-icons/lu" :as icons-lu]
   ["react-icons/md" :as icons-md]
   ["react-icons/pi" :as icons-pi]
   ["react-icons/si" :as icons-si]
   ["react-icons/tb" :as icons-tb]
   ["react-icons/ti" :as icons-ti]
   ["react-icons/vsc" :as icons-vsc]
   [moody.config.env :as env]
   [moody.nav.nav-menu-group :refer [nav-menu-group]]
   [moody.router :as router]
   [moody.tools.tools :as tools]
   [reagent.core :as r]
   [taoensso.timbre :as timbre]))

(def icon-theme-system-mode [:> icons-pi/PiDesktopLight {:size "1.5em"}])
(def icon-theme-light-mode [:> icons-bs/BsSun {:size "1.25em"}])
(def icon-theme-dark-mode [:> icons-bs/BsFillMoonFill {:size "1.25em"}])
(def icon-theme-checked [:> icons-pi/PiCheckBold {:class "text-primary m-4" :size "1.5em"}])

(def initial-settings
  {:theme (let [theme-in-ls (.getItem (.-localStorage js/window) "theme")]
            (timbre/info {:theme-in-ls theme-in-ls})
            (if (#{"dark" "light"} theme-in-ls)
              (do (.setAttribute (.. js/document -documentElement) "data-theme" (name theme-in-ls))
                  theme-in-ls)
              "system"))})

(defn nav
  []
  (let [theme-ratom (r/atom (:theme initial-settings))
        img-file-ratom (r/atom (rand-nth ["moody1-icon.jpg" "moody2-icon.jpg"]))]
    (js/console.log @theme-ratom)

    (fn []
      [:aside {:class "sidebar sidebar-fixed-left sidebar-mobile h-full justify-start max-sm:fixed max-sm:-translate-x-full"}
       [:section {:class "flex items-center p-5"}
        [:a {:class "sidebar-title flex items-center gap-3"
             :href (router/path-for :home)}
         [:img {:src (str "/img/" @img-file-ratom) :class "w-7 h-7 rounded-md"}]
         [:div {:class "flex flex-col"}
          [:span "Moody"]
          [:span {:class "text-xs font-normal text-content2"} "Dev Tools"]]
         [:div
          (when (:dev? (env/config))
            [:span {:class "badge badge-outline-secondary"} "Dev env"])]]

        [:div {:class "popover"}
         [:label {:class "popover-trigger btn btn-ghost btn-sm" :tabindex "0"}
          (case @theme-ratom
            "dark" icon-theme-dark-mode
            "light" icon-theme-light-mode
            (if (.-matches (.matchMedia js/window "(prefers-color-scheme: dark)"))
              icon-theme-dark-mode
              icon-theme-light-mode))]
         [:div
          {:class "popover-content w-screen max-w-48" :tabindex "0"}
          [:div {:class "popover-arrow"}]
          [:div {:class "overflow-hidden rounded-lg"}
           [:div {:class "relative flex flex-col text-sm"}
            [:a {:on-click #(do (reset! theme-ratom "system")
                                (.setAttribute (.. js/document -documentElement) "data-theme" "")
                                (.removeItem (.-localStorage js/window) "theme"))
                 :class "flex items-center rounded-lg ease-in-out hover:bg-gray-5 focus:outline-none focus-visible:ring focus-visible:ring-orange-500 focus-visible:ring-opacity-50"}
             [:div {:class "flex h-10 w-10 shrink-0 items-center justify-center sm:h-12 sm:w-12"}
              icon-theme-system-mode]
             [:span {:class "mr-auto"}
              "System"]
             [:span (when (= @theme-ratom "system") [:> icons-pi/PiCheckBold {:class "text-primary m-4" :size "1.5em"}])]]
            [:a {:on-click #(do (reset! theme-ratom "light")
                                (.setAttribute (.. js/document -documentElement) "data-theme" "light")
                                (.setItem (.-localStorage js/window) "theme" "light"))
                 :class "flex items-center rounded-lg ease-in-out hover:bg-gray-5 focus:outline-none focus-visible:ring focus-visible:ring-orange-500 focus-visible:ring-opacity-50"}
             [:div {:class "flex h-10 w-10 shrink-0 items-center justify-center sm:h-12 sm:w-12"}
              icon-theme-light-mode]
             [:span {:class "mr-auto"}
              "Light"]
             [:span (when (= @theme-ratom "light") [:> icons-pi/PiCheckBold {:class "text-primary m-4" :size "1.5em"}])]]
            [:a {:on-click #(do (reset! theme-ratom "dark")
                                (.setAttribute (.. js/document -documentElement) "data-theme" "dark")
                                (.setItem (.-localStorage js/window) "theme" "dark"))
                 :class " flex items-center rounded-lg ease-in-out hover:bg-gray-5 focus:outline-none focus-visible:ring focus-visible:ring-orange-500 focus-visible:ring-opacity-50"}
             [:div {:class "flex h-10 w-10 shrink-0 items-center justify-center sm:h-12 sm:w-12"}
              icon-theme-dark-mode]
             [:span {:class "mr-auto"}
              "Dark"]
             [:span (when (= @theme-ratom "dark") [:> icons-pi/PiCheckBold {:class "text-primary m-4" :size "1.5em"}])]]]]]]]

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
           (nav-menu-group :crypto "Crypto" tools/crypto-tools icons-md/MdEnhancedEncryption)
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
            [:a {:href (router/path-for :about)}
             [:div {:class "menu-item"}
              [:> icons-lu/LuStickyNote {:size "1.5em"}]
              [:span "About"]]]]
           [:li
            [:a {:href "https://github.com/hatappo/moody" :target "_blank"}
             [:div {:class "menu-item"}
              [:> icons-vsc/VscGithub {:size "1.5em"}]
              "GitHub"
              [:> icons-io5/IoOpenOutline {:size "1.0em"}]]]]]]]]])))
