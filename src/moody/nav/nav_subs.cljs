(ns moody.nav.nav-subs
  (:require
   [re-frame.core :refer [reg-sub]]))

(reg-sub
 :nav
 (fn [db _]
   (get db :nav)))

(reg-sub
 :active-nav
 :<- [:nav] ;  (fn [_] (subscribe [:nav])) と等価。省略形。
 (fn [nav _]
   (get nav :active-nav)))

(reg-sub
 :active-page
 :<- [:nav]
 (fn [nav _]
   (get nav :active-page)))
