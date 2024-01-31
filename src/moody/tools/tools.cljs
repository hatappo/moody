(ns moody.tools.tools
  (:require
   ["js-beautify" :as js-beautify]
   ["react-icons/bs" :as icons-bs]
   ["react-icons/gi" :as icons-gi]
   ["react-icons/ti" :as icons-ti]
   ["react-icons/vsc" :as icons-vsc]
   ["sql-formatter" :as sql-formatter]
   [clojure.string :as str]
   [clojure.walk :refer [prewalk]]
   [edamame.core :refer [parse-string]]
   [hickory.core :refer [as-hiccup as-hickory parse-fragment]]
   [hickory.render :refer [hickory-to-html]]
   [moody.helpers.helpers :refer [determine-data-format html? json?
                                  pprint-str
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
  (if-not (html? html)
    "parse error"
    (->> html
         strip-newline-and-tab
         parse-fragment
         (map as-hiccup)
         (map (if pretty? pprint-str pr-str))
         (str/join ""))))

(defn- convert-jsx-to-hiccup
  [jsx options]
  (if-not (html? jsx)
    "parse error"
    (-> jsx
        (convert-jsx-to-html options)
        (convert-html-to-hiccup options))))

(defn- convert-json-to-edn
  [json {:keys [pretty?]}]
  (if-not (json? json)
    "parse error"
    (-> json
        js/JSON.parse
        (js->clj :keywordize-keys true)
        ((if pretty? pprint-str pr-str)))))

(defn- convert-with-noop
  [text {:keys [pretty?]}]
  (if (= 0 (count (str/trim text)))
    ""
    (let [data-format (determine-data-format text)]
      (timbre/info {:determined-data-format data-format})
      (case data-format
        :html (str "<!-- As html code -->\n"
                   (.html js-beautify text #js {:indent_size 2 :end_with_newline true :preserve_newlines false :wrap_attributes "force-expand-multiline" :wrap_attributes_min_attrs 2}))
        :json (str "// As json code\n" (.stringify js/JSON (.parse js/JSON text) nil (when pretty? "  ")))
        :yaml "TODO: not implemented"
        :toml "TODO: not implemented"
        :xml "TODO: not implemented"
        :kdl "TODO: not implemented"
        :clj (str "; As clojure code\n" ((if pretty? pprint-str println-str) (parse-string text)))
        :sql (str "-- As sql query\n" (sql-formatter/format text #js {:keywordCase "upper" :dataTypeCase "upper" :functionCase "upper" :newlineBeforeSemicolon true}))
        :unknown (str "\nunknown data-format.\n\n" text)
        (str "error: unexpected data-format: " type)))))

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
    :convert-fn convert-json-to-edn}
   {:tool-type :noop
    :tool-category :conversion
    :tool-tags #{:others}
    :icon icons-bs/BsFiletypeRaw
    :icon-html ".... <br/> ↓ <br/> ...."
    :title "Noop"
    :label "Noop"
    :desc "It's useful to just prettify code. The data format is automatically judged"
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
