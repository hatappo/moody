(ns moody.core
  (:require
   [moody.about.about-page :refer [about-page]]
   [moody.cards.cards-page :refer [cards-page]]
   [moody.config.env :refer [config]]
   [moody.db.db]
   [moody.events]
   [moody.nav.nav :refer [nav]]
   [moody.nav.nav-events]
   [moody.nav.nav-subs]
   [moody.router :as router]
   [moody.settings.settings-page :refer [settings-page]]
   [moody.subs]
   [moody.tools.conversion-page :refer [conversion-page]]
   [moody.tools.datetime-page :refer [datetime-page]]
   [moody.tools.filetype-page :refer [filetype-page]]
   [moody.tools.hash-page :refer [hash-page]]
   [moody.tools.qr-page :refer [qr-page]]
   [moody.tools.radix-page :refer [radix-page]]
   [moody.tools.stamp-page :refer [stamp-page]]
   [moody.tools.uuid-page :refer [uuid-page]]
   [re-frame.core :as rf]
   [reagent.dom.client :as rdc]
   [taoensso.timbre :as timbre]))

(defn pages
  [page-name]
  (case page-name
    :home [cards-page]
    #_#_:cards [cards-page]
    :settings [settings-page]
    :conversion [conversion-page]
    :stamp [stamp-page]
    :uuid [uuid-page]
    :datetime [datetime-page]
    :filetype [filetype-page]
    :radix [radix-page]
    :hash [hash-page]
    :qr [qr-page]
    :about [about-page]
    [about-page]))

(defn app
  []
  (let [active-page @(rf/subscribe [:active-page])
        search-text @(rf/subscribe [:search-text])
        display-cards-page? (empty? search-text)]
    [:div {:class "flex flex-row sm:gap-10"}
     [:div {:class "sm:w-full sm:max-w-[18rem]"}
      ;;  sidebar toggle checkbox (invisible)
      [:input {:type "checkbox" :id "sidebar-mobile-fixed" :class "sidebar-state"}]
      ;;  sidebar closer on mobile
      [:label {:for "sidebar-mobile-fixed" :class "sidebar-overlay"}]
      [nav]]
     [:main {:class "flex w-full flex-col py-2"}
      [:div {:class "w-fit" :id "main-container"}
       ;;  sidebar opener on mobile
       [:label {:for "sidebar-mobile-fixed" :class "btn-primary btn sm:hidden"} "Open Sidebar"]]
      [:div {:class (when-not display-cards-page? "hidden")}
       [pages active-page]]
      [:div {:class (str (comment :class) "absolute " (when display-cards-page? "hidden"))}
       [cards-page]]]]))

(defn ^:dev/after-load start
  []
  (-> (.getElementById js/document "app")
      (rdc/create-root)
      (rdc/render [app])))

(defn ^:export init
  []
  (js/console.log (select-keys (config) [:env-id :log-level]))
  (timbre/set-min-level! (:log-level (config)))
  (rf/dispatch-sync [:initialize-db])
  (router/start!)
  (start))
