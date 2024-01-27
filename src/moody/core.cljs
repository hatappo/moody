(ns moody.core
  (:require
   [moody.cards.cards-page :refer [cards-page]]
   [moody.config.env :refer [config]]
   [moody.db]
   [moody.errors.errors-events]
   [moody.errors.errors-subs]
   [moody.events]
   [moody.feature.hiccup-page :refer [hiccup-page]]
   [moody.home.views.home-page :refer [home-page]]
   [moody.nav.nav :refer [nav]]
   [moody.nav.nav-events]
   [moody.nav.nav-subs]
   [moody.router :as router]
   [moody.subs]
   [re-frame.core :as rf]
   [reagent.dom :as dom]
   ;;  [reagent.dom.client :as rdc]
   [taoensso.timbre :as timbre]))

(defn pages
  [page-name]
  (case page-name
    :home [home-page]
    :cards [cards-page]
    :hiccup [hiccup-page]
    [home-page]))

(defn app
  []
  (let [active-page @(rf/subscribe [:active-page])]
    [:div {:class "flex flex-row sm:gap-10"}
     [:div {:class "sm:w-full sm:max-w-[18rem]"}
      ;;  sidebar toggle checkbox (invisible)
      [:input {:type "checkbox" :id "sidebar-mobile-fixed" :class "sidebar-state"}]
      ;;  sidebar closer on mobile
      [:label {:for "sidebar-mobile-fixed" :class "sidebar-overlay"}]
      [nav]]
     [:main {:class "flex w-full flex-col p-4"}
      [:div {:class "w-fit"}
       ;;  sidebar opener on mobile
       [:label {:for "sidebar-mobile-fixed", :class "btn-primary btn sm:hidden"} "Open Sidebar"]]
      [pages active-page]]]))

(defn ^:dev/after-load start
  []
  (dom/render [app] (.getElementById js/document "app")))

(defn ^:export init
  []
  (rf/dispatch-sync [:initialize-db])
  (timbre/set-min-level! (-> (config) :log-level))
  (router/start!)
  (js/console.log router/routes)
  (start))
