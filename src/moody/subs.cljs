(ns moody.subs
  (:require
   [re-frame.core :refer [reg-sub]]))

(reg-sub
 :options
 (fn [db _]
   (get-in db [:conversion :options])))

(reg-sub
 :input-text
 (fn [db _]
   (get-in db [:conversion :input-text])))

(reg-sub
 :output-text
 (fn [db _]
   (get-in db [:conversion :output-text])))

(reg-sub
 :editor-theme
 (fn [db _]
   (get-in db [:editor :theme])))

(reg-sub
 :editor-word-wrap
 (fn [db _]
   (get-in db [:editor :word-wrap])))

(reg-sub
 :editor-whitespace-option
 (fn [db _]
   (get-in db [:editor :whitespace-option])))
