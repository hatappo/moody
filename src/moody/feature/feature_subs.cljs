(ns moody.feature.feature-subs
  (:require
   [re-frame.core :refer [reg-sub]]))

(reg-sub
 :input-text
 (fn [db _]
   (get-in db [:conversion :input-text])))

(reg-sub
 :output-text
 (fn [db _]
   (get-in db [:conversion :output-text])))
