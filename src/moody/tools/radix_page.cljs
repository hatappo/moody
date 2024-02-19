(ns moody.tools.radix-page
  (:require
   [clojure.string :as str]
   [reagent.core :as r]))

(defn format-number-text
  [s re separator]
  (->> s
       str/reverse
       (re-seq re)
       (str/join separator)
       str/reverse))

(def radices
  [{:base 10 :re #".{1,3}" :separator "," :title "Decimal"}
   {:base 16 :re #".{1,4}" :separator " " :title "Hexadecimal"}
   {:base 8 :re #".{1,3}" :separator " " :title "Octal"}
   {:base 2 :re #".{1,4}" :separator " " :title "Binary"}])

(def initial-values
  {:num 1048575
   :upper false
   :format true})

(defn radix-page
  []
  (let [num-ratom (r/atom (:num initial-values))
        upper-ratom (r/atom (:upper initial-values))
        format-ratom (r/atom (:format initial-values))]
    (fn []
      [:article {:class "flex flex-col gap-2"}
       [:h2 {:class "text-xl my-4"}
        "Radix Converter"]

       [:section {:class "flex flex-col gap-8 pr-8"}

        [:div {:class "flex flex-col gap-2 pr-8"}

         [:div {:class "flex items-center gap-2"}
          [:label {:for "format-checkbox"}
           "Format:"]
          [:input {:class "switch switch-bordered-primary mr-2"
                   :type "checkbox"
                   :id "format-checkbox"
                   :checked @format-ratom
                   :on-change (fn [_e] (swap! format-ratom not))}]]

         [:div {:class "flex items-center gap-2"}
          [:label {:for "upper-checkbox"}
           "Upper case:"]
          [:input {:class "switch switch-bordered-primary mr-2"
                   :type "checkbox"
                   :id "upper-checkbox"
                   :checked @upper-ratom
                   :on-change (fn [_e] (swap! upper-ratom not))}]]]

        [:div {:class "flex flex-col gap-4"}
         (doall
          (map (fn [{:keys [base title re separator]}]
                 (let [html-id (str "radix-" title "-input")]
                   ^{:key title}
                   [:div {:class "flex flex-col gap-1"}
                    [:label {:for html-id} (str title " (" base ")")]
                    [:input {:class "input input-sm input-block input-ghost-primary text-right"
                             :type "text"
                             :value (let [n (js/Number.parseInt @num-ratom 10)] ; hold ratom value as decimal (10)
                                      (when-not (NaN? n)
                                        (-> (.toString n base)
                                            (cond-> @format-ratom (format-number-text re separator))
                                            (cond-> @upper-ratom str/upper-case))))
                             :on-change #(reset! num-ratom (js/Number.parseInt (str/replace (.. % -target -value) #"[ ,]" "") base))
                             :id html-id}]]))
               radices))]]])))
