(ns moody.errors.errors-subs
  (:require
   [re-frame.core :refer [reg-sub]]))

(reg-sub
 :info
 (fn [db _]
   (:info db)))

(reg-sub
 :errors
 (fn [db _]
   (:errors db)))

(reg-sub
 :loading
 (fn [db _]
   (:loading db)))
