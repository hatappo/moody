(ns moody.config.env-local)

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}

(defn config
  []
  {:id :local
   :log-level :trace
   :local? true
   :gh? false})
