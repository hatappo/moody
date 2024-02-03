(ns moody.tools.editor)

(def themes
  [{:val "vs"}
   {:val "vs-dark"}
   {:val "hc-black"}
   {:val "hc-light"}])

(defn word-wrap-val
  [^boolean word-wrap?]
  (if word-wrap? "bounded" "off"))

(def whitespace-options ["all" "none" "boundary" "selection" "trailing"])
