(ns moody.tools.tools
  (:require
   ["react-icons/bs" :as icons-bs]
   ["react-icons/gi" :as icons-gi]
   ["react-icons/ti" :as icons-ti]
   ["react-icons/vsc" :as icons-vsc]
   [clojure.string :as str]
   [clojure.walk :refer [prewalk]]
   [expound.printer :refer [pprint-str]]
   [hickory.core :refer [as-hiccup as-hickory parse-fragment]]
   [hickory.render :refer [hickory-to-html]]
   [moody.helpers.helpers :refer [determine-data-structure-type
                                  rename-jsx-specific-attrs-to-html-attrs
                                  strip-newline-and-tab]]
   [taoensso.timbre :as timbre]))

(defn- convert-jsx-to-html
  [jsx _options]
  (->> jsx
       strip-newline-and-tab
       parse-fragment
       (map as-hickory)
       (prewalk (fn [node] (if (map? node) (rename-jsx-specific-attrs-to-html-attrs node) node)))
       (map hickory-to-html)
       (str/join "")))

(defn- convert-html-to-hiccup
  [html {:keys [pretty?]}]
  (->> html
       strip-newline-and-tab
       parse-fragment
       (map as-hiccup)
       (map (if pretty? pprint-str pr-str))
       (str/join "")))

(defn- convert-jsx-to-hiccup
  [jsx options]
  (-> jsx
      (convert-jsx-to-html options)
      (convert-html-to-hiccup options)))

(defn- convert-with-noop
  [text {:keys [pretty?]}]
  (let [type (determine-data-structure-type text)]
    (case type
      :json "TODO: not implemented"
      :yaml "TODO: not implemented"
      :toml "TODO: not implemented"
      :xml "TODO: not implemented"
      :kdl "TODO: not implemented"
      :edn ((if pretty? pprint-str println-str) text)
      :hiccup ((if pretty? pprint-str println-str) text)
      text)))

(declare convert)

(declare tools-by-tool-type)

(defn- convert-anything-to-something
  [text options]
  (let [tool-type (-> tools-by-tool-type (dissoc :roulette) keys rand-nth)]
    (timbre/info {:randomly-selected-tool-type tool-type})
    (convert text options tool-type)))

(defn convert
  [text options tool-type]
  (let [{:keys [convert-fn]} (get tools-by-tool-type (keyword tool-type) #(str "error: unexpected tool-type: " tool-type))]
    (convert-fn text options)))

(defrecord Tool
  [tool-type     ; used in url path, as unique identical name
   tool-category ; used in url path
   tool-tags     ; used to grouping tools in `sidebar`
   icon
   icon-html
   title         ; used as card title in `all-tools` page
   label         ; used as dropdown text in `conversion` page
   desc
   convert-fn])

(def tools
  [{:tool-type :html2hiccup
    :tool-category :conversion
    :tool-tags #{:clojure :html :hiccup}
    :icon icons-ti/TiHtml5
    :icon-html "&lt;html&gt; <br/> ↓ <br/> [:hiccup]"
    :title "HTML - Hiccup"
    :label "HTML -> Hiccup (Clojure)"
    :desc "Convert HTML and Hiccup to each other"
    :convert-fn convert-html-to-hiccup}
   {:tool-type :jsx2hiccup
    :tool-category :conversion
    :tool-tags #{:clojure :jsx :hiccup}
    :icon icons-bs/BsFiletypeJsx
    :icon-html "&lt;jsx&gt; <br/> ↓ <br/> [:hiccup]"
    :title "JSX - Hiccup"
    :label "JSX -> Hiccup (Clojure)"
    :desc "Convert JSX and Hiccup to each other"
    :convert-fn convert-jsx-to-hiccup}
   {:tool-type :json2edn
    :tool-category :conversion
    :tool-tags #{:clojure :json :edn}
    :icon icons-vsc/VscJson
    :icon-html "{\"json\": []} <br/> ↓ <br/> {:edn []}"
    :title "JSON - EDN"
    :label "JSON -> EDN (Clojure)"
    :desc "Convert JSON and EDN to each other"
    :convert-fn (fn [_json _options] "TODO: not implemented convert-fn")}
   {:tool-type :noop
    :tool-category :conversion
    :tool-tags #{:others}
    :icon icons-bs/BsFiletypeRaw
    :icon-html ".... <br/> ↓ <br/> ...."
    :title "Noop"
    :label "Noop"
    :desc "Nothing. It can be used when you just want to format data, etc."
    :convert-fn convert-with-noop}
   {:tool-type :roulette
    :tool-category :conversion
    :tool-tags #{:others}
    :icon icons-gi/GiCardRandom
    :icon-html "??? <br/> ↓ <br/> ???"
    :title "Roulette"
    :label "Roulette"
    :desc ""
    :convert-fn convert-anything-to-something}])

(def tools-by-tool-type
  (zipmap (map :tool-type tools) tools))

(def conversion-tools
  (filter #(= :conversion (:tool-category %)) tools))

(def clojure-tools
  (filter #(:clojure (:tool-tags %)) tools))
