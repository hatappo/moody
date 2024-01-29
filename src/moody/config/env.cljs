(ns moody.config.env)

(goog-define moody-env "disabled default value to be replaced with shadow-cljs in compile time")

(defn config
  []
  (case moody-env

    "DEV"
    {:env-id :dev
     :log-level :info
     :dev? true
     :gh? false}

    "PROD"
    {:env-id :github-pages
     :log-level :warn
     :dev? false
     :github-pages? true}

    (js/Error. "Illegal moody-env value: " moody-env)))
