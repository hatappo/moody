(ns moody.config.env-github-pages)

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}

(defn config
  []
  {:id :github-pages
   :log-level :warn
   :local? false
   :github-pages? true})
