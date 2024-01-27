(ns moody.subs
  (:require
   [re-frame.core :refer [reg-sub]]))

(reg-sub
 :feature-summaries
 (fn [db _]
   (get-in db [:master :feature-summaries])))

(reg-sub
 :input-text
 (fn [db _]
   (get-in db [:conversion :input-text])))

(reg-sub
 :output-text
 (fn [db _]
   (get-in db [:conversion :output-text])))
