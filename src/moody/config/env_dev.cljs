(ns moody.config.env-dev)

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}

(defn config
  []
  {:id :dev
   :log-level :info
   :dev? true
   :gh? false})
