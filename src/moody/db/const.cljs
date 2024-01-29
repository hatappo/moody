(ns moody.db.const
  (:require
   ["react-icons/bs" :as icons-bs]
   ["react-icons/vsc" :as icons-vsc]))

(def input-data-types
  [{:value "html" :label "HTML"} {:value "jsx" :label "JSX"}])

(def page-summaries
  [{:id :hiccup #_#_:icon icons-bs/BsFiletypeHtml :code-let "<html>" :title "HTML - Hiccup" :desc "Convert HTML and Hiccup to each other"}
   {:id :edn #_#_:icon icons-vsc/VscJson :code-let "{\"\": 0}" :title "JSON - EDN" :desc "Convert JSON and EDN to each other"}
   {:id :other1 :icon icons-vsc/VscCopilot :code-let "・・・" :title "・・・・・1" :desc ".... .... .... .... .... .... .... .... .... .... .... .... "}
   {:id :other2 :icon icons-bs/BsThreeDots :code-let "・・・" :title "・・・・・2" :desc ".... .... .... .... .... .... .... .... .... .... .... .... "}])
