(ns moody.config.env)

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}

(defn config
  []
  {:id :none
   :log-level :trace
   :local? false
   :gh? false})
