(ns moody.tools.tools
  (:require
   ["js-beautify" :as js-beautify]
   ["react-icons/bs" :as icons-bs]
   ["react-icons/gi" :as icons-gi]
   ["react-icons/go" :as icons-go]
   ["react-icons/pi" :as icons-pi]
   ["react-icons/si" :as icons-si]
   ["react-icons/tb" :as icons-tb]
   ["react-icons/ti" :as icons-ti]
   ["react-icons/vsc" :as icons-vsc]
   ["sql-formatter" :as sql-formatter]
   [clojure.set :refer [union]]
   [clojure.string :as str]
   [clojure.walk :refer [prewalk]]
   [edamame.core :refer [parse-string]]
   [hickory.core :refer [as-hiccup as-hickory parse-fragment]]
   [hickory.render :refer [hickory-to-html]]
   [moody.helpers.helpers :refer [determine-data-format html? json?
                                  pprint-str
                                  rename-jsx-specific-attrs-to-html-attrs
                                  strip-newline-and-tab]]
   [moody.tools.encoding :refer [base64-decode base64-encode]]
   [taoensso.timbre :as timbre]
   [testdouble.cljs.csv :as csv]))

(def html-pretty-print-options
  #js {:indent_size 2 :end_with_newline true :preserve_newlines false :wrap_attributes "force-expand-multiline" :wrap_attributes_min_attrs 2})

(defn html->noop [html {:keys [pretty?]}] (if-not pretty? html (.html js-beautify html html-pretty-print-options)))

(defn jsx->noop [& args] (apply html->noop args))

(defn json->noop [json {:keys [pretty?]}]
  (cond
    (not (json? json)) json
    (not pretty?) json
    :else (.stringify js/JSON (.parse js/JSON json) nil "  ")))

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

(defn csv->analysis
  [text]
  (let [c (csv/read-csv (str/trim text))
        headers (first c)]
    (->
     {"All records" (count c)
      "Max columns" (->> c
                         ((partial map count))
                         (apply max))
      "Min columns" (->> c
                         ((partial map count))
                         (apply min))
      "Are headers unique" (= (count headers)
                              (count (set headers)))
      "Duplicated headers" (->> headers
                                ((partial group-by identity))
                                seq
                                (filter (fn [[_ headers]] (< 1 (count headers))))
                                (map first))}
     clj->js
     (js/JSON.stringify nil "  "))))

(defn plain->base64 [plain-text] (base64-encode plain-text))

(defn base64->plain [encoded-text] (base64-decode encoded-text))

(defn plain->uri [plain-text] (js/encodeURI plain-text))

(defn uri->plain [encoded-text] (js/decodeURI encoded-text))

(defn html->escaped-html-hex
  [plain-text]
  (str/escape plain-text {\& "&#x26;" \' "&#x27;" \" "&#x22;" \< "&#x3C;" \> "&#x3E;"}))

(defn html->escaped-html [plain-text] (html->escaped-html-hex plain-text))

(defn escaped-html->html
  [escaped-text]
  (-> escaped-text
      ;; hexadecimal ref | decimal ref | character ref
      (str/replace #"(&#x26;|&#38;|&amp;)" "&")
      (str/replace #"(&#x27;|&#39;|&apos;)" "'")
      (str/replace #"(&#x22;|&#34;|&quot;)" "\"")
      (str/replace #"(&#x3C;|&#60;|&lt;)" "<")
      (str/replace #"(&#x3E;|&#62;|&gt;)" ">")))

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
    :title nil
    :label " - "
    :editor-lang "plaintext"
    :icon nil
    :icon-html nil
    :tags #{}
    :example ""
    :convert-fns {}}
   {:notation-type :analysis
    :title "Analysis"
    :label "Result of analysis"
    :editor-lang "json"
    :icon nil
    :icon-html "📊"
    :tags #{}
    :example ""
    :convert-fns {}}
   {:notation-type :html
    :title "HTML"
    :label "HTML"
    :icon icons-ti/TiHtml5
    :icon-html "&lt;html&gt;"
    :editor-lang "html"
    :tags #{:html}
    :example "<a href=\"/foo/bar#\">hello</a>"
    :convert-fns {:noop html->noop
                  :hiccup html->hiccup}}
   {:notation-type :jsx
    :title "JSX"
    :label "JSX"
    :editor-lang "html"
    :icon icons-bs/BsFiletypeJsx
    :icon-html "&lt;jsx&gt;"
    :tags #{:jsx :html :javascript}
    :example "<a href=\"/foo/bar#\" className=\"btn-primary\">hello</a>"
    :convert-fns {:noop jsx->noop
                  :hiccup jsx->hiccup}}
   {:notation-type :hiccup
    :title "Hiccup"
    :label "Hiccup (Clojure)"
    :editor-lang "clojure"
    :icon icons-si/SiClojure
    :icon-html "[:hiccup]"
    :tags #{:clojure :html}
    :example "[:a {:href \"/foo/bar#\"} \"hello\"]"
    :convert-fns {:noop hiccup->noop}}
   {:notation-type :edn
    :title "EDN"
    :label "EDN (Clojure)"
    :editor-lang "clojure"
    :icon icons-si/SiClojure
    :icon-html "{:edn []}"
    :tags #{:clojure :conf}
    :example ""
    :convert-fns {:noop edn->noop}}
   {:notation-type :json
    :title "JSON"
    :label "JSON"
    :editor-lang "json"
    :icon icons-vsc/VscJson
    :icon-html "{\"json\": []}"
    :tags #{:json :conf :javascript}
    :example ""
    :convert-fns {:noop json->noop
                  :edn json->edn}}
   {:notation-type :yaml
    :title "YAML"
    :label "YAML"
    :editor-lang "yaml"
    :icon icons-si/SiYaml
    :icon-html "- {yaml: \"\"}"
    :tags #{:yaml :conf}
    :example ""
    :convert-fns {}}
   {:notation-type :toml
    :title "TOML"
    :label "TOML"
    :editor-lang "plaintext"
    :icon icons-si/SiToml
    :icon-html "[T]"
    :tags #{:toml :conf}
    :example ""
    :convert-fns {}}
   {:notation-type :xml
    :title "XML"
    :label "XML"
    :editor-lang "xml"
    :icon icons-bs/BsFiletypeXml
    :icon-html "&lt;xml&gt;"
    :tags #{:xml :conf}
    :example ""
    :convert-fns {}}
   {:notation-type :kdl
    :title "KDL"
    :label "KDL"
    :editor-lang "plaintext"
    :icon icons-tb/TbSquareLetterK
    :icon-html "/-kdl {}"
    :tags #{:kdl :conf}
    :example ""
    :convert-fns {}}
   {:notation-type :pkl
    :title "Pkl"
    :label "Pkl"
    :editor-lang "plaintext"
    :icon icons-tb/TbSquareLetterP
    :icon-html "pkl {}"
    :tags #{:pkl :conf :apple}
    :example ""
    :convert-fns {}}
   {:notation-type :csv
    :title "CSV"
    :label "CSV"
    :editor-lang "plaintext"
    :icon icons-bs/BsFiletypeCsv
    :icon-html "foo,bar,baz"
    :tags #{:table}
    :example "\"ID\",\"First Name\",\"Family Name\",\"age\",\"ID\"\n\"1\",\"Taro\",\"Suzuki\",\"24\",\"A-1\"\n\"2\",\"John\",\"Yamada\",\"24\",\"B-2\"\n\"3\",\"Yu\",\"Ito\",\"24\",\"C-3\""
    :convert-fns {:analysis csv->analysis}}
   {:notation-type :sql
    :title "SQL"
    :label "SQL"
    :editor-lang "sql"
    :icon icons-bs/BsFiletypeSql
    :icon-html "SELECT *"
    :tags #{:sql :db}
    :example ""
    :convert-fns {:noop sql->noop}}
   {:notation-type :plain
    :title "Plain Text"
    :label "plain text"
    :editor-lang "plaintext"
    :icon icons-bs/BsAlphabet
    :icon-html "a b c"
    :tags #{}
    :example "a b c"
    :convert-fns {:base64 plain->base64
                  :uri plain->uri
                  :escaped-html plain->escaped-html}}
   {:notation-type :base64
    :title "Base 64"
    :label "Base64"
    :editor-lang "plaintext"
    :icon icons-pi/PiEquals
    :icon-html "YSBiIGM="
    :tags #{:encoding}
    :example "YSBiIGM="
    :convert-fns {:plain base64->plain}}
   {:notation-type :uri
    :title "URI"
    :label "URI(URL)"
    :editor-lang "plaintext"
    :icon icons-go/GoLink
    :icon-html "a%20b%20c"
    :tags #{:encoding}
    :example "https://example.com/index.html?icon=%F0%9F%98%8B"
    :convert-fns {:plain uri->plain}}
   {:notation-type :escaped-html
    :title "Escaped HTML"
    :label "Escaped HTML"
    :editor-lang "plaintext"
    :icon icons-bs/BsFiletypeHtml
    :icon-html "&amp;lt;a&amp;gt;" ; <= (escape (escape "<a>"))
    :tags #{:encoding :html}
    :example "&#x3C;script&#x3E;alert(&#x22;123&#x22;)&#x3C;/script&#x3E;"
    :convert-fns {:plain escaped-html->plain}}
   {:notation-type :auto
    :title "Auto"
    :label "Auto"
    :editor-lang "plaintext"
    :icon icons-bs/BsRobot
    :icon-html "🤖"
    :tags #{:others :experimental}
    :example ""
    :convert-fns {:noop auto->noop}}
   {:notation-type :roulette
    :title "Roulette"
    :label "Roulette"
    :editor-lang "plaintext"
    :icon icons-gi/GiCardRandom
    :icon-html "❓"
    :tags #{:others :experimental}
    :example ""
    :convert-fns {:noop roulette->noop}}])

(def notations-by-notation-type
  (zipmap (map :notation-type notations) notations))

(def ^:private tools-raw
  (->> notations
       (map (fn [{in-type :notation-type
                  in-title :title
                  in-label :label
                  in-tags :tags
                  in-icon :icon
                  in-icon-html :icon-html
                  convert-fns :convert-fns}]
              (map (fn [[output-notation-type _]]
                     (let [{out-type :notation-type
                            out-title :title
                            out-label :label
                            out-tags :tags
                            out-icon-html :icon-html}
                           (output-notation-type notations-by-notation-type)]
                       {:tool-type (keyword (str (name in-type) "->" (name out-type)))
                        :input-type in-type
                        :output-type out-type
                        :title (cond
                                 (:encoding out-tags) out-label
                                 (:encoding in-tags) (str "Decode " in-label)
                                 out-title (str in-title " -> " out-title)
                                 :else in-title)
                        :label (str in-title " -> " (if out-title out-title in-title))
                        :input-tags in-tags
                        :output-tags out-tags
                        :tool-tags (union in-tags out-tags)
                        :icon in-icon
                        :icon-html (if out-icon-html (str in-icon-html " <br/> ↓ <br/> "  out-icon-html) in-icon-html)
                        :desc (cond
                                (= in-type :roulette) ""
                                (= in-type :auto) "Automatically detected the data type and format it."
                                (= out-type :noop) "Format only."
                                (:encoding out-tags) (str "Encode text to " out-label ".")
                                (:encoding in-tags) (str "Decode " in-label " encoded text")
                                :else (str "Transform " in-label " data into " out-label " style."))}))
                   (seq convert-fns))))
       flatten
       (sort-by #(= :noop (:output-type %)))))

(defn assoc-relevant-words-text
  [tools-raw]
  (map (fn [{:keys [title label desc tool-tags] :as tool}]
         (let [tag-words-text (str/join tool-tags)
               relevant-words-text (str "\n" title "\n" label "\n" desc "\n" tag-words-text)]
           (assoc tool :relevant-words-text (str/lower-case relevant-words-text))))
       tools-raw))

(def tools (assoc-relevant-words-text tools-raw))

(def clojure-tools
  (filter #(and (:clojure (:tool-tags %)) (not= (:output-type %) :noop)) tools))

(def html-tools
  (filter #(and (:html (:tool-tags %)) (not= (:output-type %) :noop)) tools))

(def encoding-tools
  (filter #(and (:encoding (:tool-tags %)) (not= (:output-type %) :noop)) tools))

(def table-tools
  (filter #(and (:table (:tool-tags %)) (not= (:output-type %) :noop)) tools))
