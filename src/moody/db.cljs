(ns moody.db
  (:require
   ["react-icons/bs" :as icons-bs]
   ["react-icons/vsc" :as icons-vsc]
   [day8.re-frame.tracing-stubs :refer-macros [fn-traced]]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(def master-db
  {:feature-summaries
   [{:id :hiccup #_#_:icon icons-bs/BsFiletypeHtml :code-let "<html>" :title "HTML - Hiccup" :desc "Convert HTML and Hiccup to each other"}
    {:id :edn #_#_:icon icons-vsc/VscJson :code-let "{\"\": 0}" :title "JSON - EDN" :desc "Convert JSON and EDN to each other"}
    {:id :other1 :icon icons-vsc/VscCopilot :code-let "・・・" :title "・・・・・1" :desc ".... .... .... .... .... .... .... .... .... .... .... .... "}
    {:id :other2 :icon icons-bs/BsThreeDots :code-let "・・・" :title "・・・・・2" :desc ".... .... .... .... .... .... .... .... .... .... .... .... "}]

   :foo "bar"})

(def initial-app-db
  {:master master-db
   :conversion {:input-text "<a href=\"/foo/bar#\">あいうえお</a>"
                :output-text "[:a {:href \"/for/bar#\"} \"あいうえお\"]"}
   :nav {:active-page nil}})

(rf/reg-event-db
 :initialize-db
 (fn-traced [_db]
            initial-app-db))
