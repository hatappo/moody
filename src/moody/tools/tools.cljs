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

(def html-pretty-print-options
  #js {:indent_size 2 :end_with_newline true :preserve_newlines false :wrap_attributes "force-expand-multiline" :wrap_attributes_min_attrs 2})

(defn html->noop [html {:keys [pretty?]}] (if-not pretty? html (.html js-beautify html html-pretty-print-options)))

(defn jsx->noop [& args] (apply html->noop args))

(defn json->noop [json {:keys [pretty?]}] (if-not pretty? json (.stringify js/JSON (.parse js/JSON json) nil "  ")))

(defn edn->noop [edn {:keys [pretty?]}] (if-not pretty? edn (pprint-str (parse-string edn))))

(defn hiccup->noop [& args] (apply edn->noop args))

(def sql-pretty-print-options
  #js {:keywordCase "upper" :dataTypeCase "upper" :functionCase "upper" :newlineBeforeSemicolon true})

(defn sql->noop [sql {:keys [pretty?]}] (if-not pretty? sql (sql-formatter/format sql sql-pretty-print-options)))

(defn html->hiccup
  [html {:keys [pretty?]}]
  (if-not (html? html)
    "parse error"
    (->> html
         strip-newline-and-tab
         parse-fragment
         (map as-hiccup)
         (map (if pretty? pprint-str pr-str))
         (str/join ""))))

(defn jsx->html
  [jsx _]
  (->> jsx
       strip-newline-and-tab
       parse-fragment
       (map as-hickory)
       (prewalk (fn [node] (if (map? node) (rename-jsx-specific-attrs-to-html-attrs node) node)))
       (map hickory-to-html)
       (str/join "")))

(defn jsx->hiccup
  [jsx options]
  (-> jsx
      (jsx->html (merge options {:input-type :jsx :output-type :html}))
      (html->hiccup (merge options {:input-type :html :output-type :hiccup}))))

(defn json->edn
  [json {:keys [pretty?]}]
  (if-not (json? json)
    "parse error"
    (-> json
        js/JSON.parse
        (js->clj :keywordize-keys false #_true) ; TODO:
        ((if pretty? pprint-str pr-str)))))

(defn auto->noop
  [& args]
  (let [data-format (determine-data-format (first args))]
    (timbre/info {:fn :auto->noop :determined-data-format data-format :args args})
    (case data-format
      :html (str "<!-- As html code -->\n" (apply html->noop args))
      :json (str "// As json code\n" (apply json->noop args))
      :yaml "TODO: not implemented"
      :toml "TODO: not implemented"
      :xml "TODO: not implemented"
      :kdl "TODO: not implemented"
      :pkl "TODO: not implemented"
      :clj (str "; As clojure code\n" (apply edn->noop args))
      :sql (str "-- As sql query\n" (apply sql->noop args))
      :unknown (str "\nunknown data-format.")
      (str "error: unexpected data-format: " type))))

(declare notations-by-notation-type)

(declare notations)

(defn- roulette->noop
  [text options]
  (let [notations (filter (fn [{:keys [convert-fns notation-type]}]
                            (and (pos? (count convert-fns))
                                 (not= notation-type :roulette)))
                          notations)
        {:keys [convert-fns] :as notation} (rand-nth notations)
        convert-fn-name (rand-nth (keys convert-fns))]
    (timbre/info {:notation notation :convert-fn convert-fn-name})
    ((convert-fn-name convert-fns) text options)))

(defn convert
  [text {:keys [input-type output-type] :as options}]
  (when-not (str/blank? text)
    (let [{:keys [convert-fns]} (notations-by-notation-type (keyword input-type))
          convert-fn (convert-fns (keyword output-type))]
      (convert-fn text options))))

(def notations
  [{:notation-type :noop
    :label " - "
    :editor-lang "plaintext"
    :convert-fns {}}
   {:notation-type :html
    :label "HTML"
    :editor-lang "html"
    :convert-fns {:noop html->noop
                  :hiccup html->hiccup}}
   {:notation-type :jsx
    :label "JSX"
    :editor-lang "html"
    :convert-fns {:noop jsx->noop
                  :hiccup jsx->hiccup}}
   {:notation-type :hiccup
    :label "Hiccup (Clojure)"
    :editor-lang "clojure"
    :convert-fns {:noop hiccup->noop}}
   {:notation-type :edn
    :label "EDN (Clojure)"
    :editor-lang "clojure"
    :convert-fns {:noop edn->noop}}
   {:notation-type :json
    :label "JSON"
    :editor-lang "json"
    :convert-fns {:noop json->noop
                  :edn json->edn}}
   {:notation-type :yaml
    :label "YAML"
    :editor-lang "yaml"
    :convert-fns {}}
   {:notation-type :toml
    :label "TOML"
    :editor-lang "plaintext"
    :convert-fns {}}
   {:notation-type :xml
    :label "XML"
    :editor-lang "xml"
    :convert-fns {}}
   {:notation-type :kdl
    :label "KDL"
    :editor-lang "plaintext"
    :convert-fns {}}
   {:notation-type :pkl
    :label "Pkl"
    :editor-lang "plaintext"
    :convert-fns {}}
   {:notation-type :sql
    :label "SQL"
    :editor-lang "sql"
    :convert-fns {:noop sql->noop}}
   {:notation-type :auto
    :label "Auto"
    :editor-lang "plaintext"
    :convert-fns {:noop auto->noop}}
   {:notation-type :roulette
    :label "Roulette"
    :editor-lang "plaintext"
    :convert-fns {:noop roulette->noop}}])

(def notations-by-notation-type
  (zipmap (map :notation-type notations) notations))

(def ^{:private true} tools-raw
  [{:tool-type :html2hiccup
    :input-type :html
    :output-type :hiccup
    :tool-tags #{:clojure :html :hiccup}
    :icon icons-ti/TiHtml5
    :icon-html "&lt;html&gt; <br/> ↓ <br/> [:hiccup]"
    :title "HTML - Hiccup"
    :label "HTML -> Hiccup (Clojure)"
    :desc "Convert HTML and Hiccup to each other"}
   {:tool-type :jsx2hiccup
    :input-type :jsx
    :output-type :hiccup
    :tool-tags #{:clojure :jsx :hiccup}
    :icon icons-bs/BsFiletypeJsx
    :icon-html "&lt;jsx&gt; <br/> ↓ <br/> [:hiccup]"
    :title "JSX - Hiccup"
    :label "JSX -> Hiccup (Clojure)"
    :desc "Convert JSX and Hiccup to each other"}
   {:tool-type :json2edn
    :input-type :json
    :output-type :edn
    :tool-tags #{:clojure :json :edn}
    :icon icons-vsc/VscJson
    :icon-html "{\"json\": []} <br/> ↓ <br/> {:edn []}"
    :title "JSON - EDN"
    :label "JSON -> EDN (Clojure)"
    :desc "Convert JSON and EDN to each other"}
   {:tool-type :auto
    :input-type :auto
    :output-type :noop
    :tool-tags #{:others}
    :icon icons-bs/BsFiletypeRaw
    :icon-html ".... <br/> ↓ <br/> ...."
    :title "Auto"
    :label "Auto"
    :desc "It's useful to just prettify (format) code. The data format is automatically judged"}
   {:tool-type :roulette
    :input-type :roulette
    :output-type :noop
    :tool-tags #{:others}
    :icon icons-gi/GiCardRandom
    :icon-html "??? <br/> ↓ <br/> ???"
    :title "Roulette"
    :label "Roulette"
    :desc ""}])

(defn assoc-relevant-words-text
  [tools-raw]
  (map (fn [{:keys [title label desc tool-tags] :as tool}]
         (let [tag-words-text (str/join tool-tags)
               relevant-words-text (str "\n" title "\n" label "\n" desc "\n" tag-words-text)]
           (assoc tool :relevant-words-text (str/lower-case relevant-words-text))))
       tools-raw))

(def tools (assoc-relevant-words-text tools-raw))

(def clojure-tools
  (filter #(:clojure (:tool-tags %)) tools))
