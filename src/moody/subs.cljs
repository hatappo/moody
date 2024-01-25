(ns moody.subs
  (:require
   [re-frame.core :refer [reg-sub]]))

(reg-sub
 :feature-summaries
 (fn [db _]
   (get-in db [:master :feature-summaries])))
