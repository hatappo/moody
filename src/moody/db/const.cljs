(ns moody.db.const
  (:require
   ["react-icons/bs" :as icons-bs]
   ["react-icons/ti" :as icons-ti]
   ["react-icons/vsc" :as icons-vsc]))

(def tools
  [{:tool-type :html2hiccup
    :tool-category :conversion
    :tool-tags #{:clojure :html :hiccup}
    :icon icons-ti/TiHtml5
    :icon-html "&lt;html&gt; <br/> ↓ <br/> [:hiccup]"
    :title "HTML - Hiccup"
    :label "HTML -> Hiccup (Clojure)"
    :desc "Convert HTML and Hiccup to each other"}
   {:tool-type :jsx2hiccup
    :tool-category :conversion
    :tool-tags #{:clojure :jsx :hiccup}
    :icon icons-bs/BsFiletypeJsx
    :icon-html "&lt;jsx&gt; <br/> ↓ <br/> [:hiccup]"
    :title "JSX - Hiccup"
    :label "JSX -> Hiccup (Clojure)"
    :desc "Convert JSX and Hiccup to each other"}
   {:tool-type :json2edn
    :tool-category :conversion
    :tool-tags #{:clojure :json :edn}
    :icon icons-vsc/VscJson
    :icon-html "{\"json\": []} <br/> ↓ <br/> {:edn []}"
    :title "JSON - EDN"
    :label "JSON -> EDN (Clojure)"
    :desc "Convert JSON and EDN to each other"}
   {:tool-type :other1
    :tool-category :home
    :tool-tags #{:other}
    :icon icons-vsc/VscCopilot
    :icon-html "other1"
    :title "・・・・・1"
    :label "other1"
    :desc ".... .... .... .... .... .... .... ...."}
   {:tool-type :other2
    :tool-category :home
    :tool-tags #{:other}
    :icon icons-bs/BsThreeDots
    :icon-html "other2"
    :title "・・・・・2"
    :label "other2"
    :desc ".... .... .... .... .... .... .... ...."}])

(def conversion-tools
  (filter (fn [{:keys [tool-category]}] (= tool-category :conversion)) tools))

(def clojure-tools
  (filter (fn [{:keys [tool-tags]}] (tool-tags :clojure)) tools))
