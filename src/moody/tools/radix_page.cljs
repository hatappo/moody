(ns moody.tools.radix-page
  (:require
   [clojure.string :as str]
   [reagent.core :as r]))

(defn radix-page
  []
  (let [num-ratom (r/atom 1048576)
        format-ratom (r/atom true)]
    (fn []
      [:article {:class "flex flex-col gap-2"}
       [:h2 {:class "text-xl my-4"}
        "Data Format Conversion - Radix Converter"]

       [:section {:class "flex flex-col gap-6 pr-8"}

        [:div {:class "flex items-center gap-2"}
         [:label {:for "format-checkbox"}
          "Format"]
         [:input {:class "switch switch-bordered-primary mr-2"
                  :type "checkbox"
                  :id "format-checkbox"
                  :checked @format-ratom
                  :on-change (fn [_e] (swap! format-ratom not))}]]

        [:div {:class "flex flex-col gap-6"}
         (doall
          (map (fn [{:keys [base title re separator]}]
                 (let [html-id (str "radix-" title "-input")]
                   ^{:key title}
                   [:div {:class "flex flex-col gap-2"}
                    [:label {:for html-id} (str title " (" base ")")]
                    [:input {:class "input input-solid input-sm input-block input-ghost-primary text-right"
                             :type "text"
                             :value (let [n (js/Number.parseInt @num-ratom 10) ; hold ratom value as decimal (10)
                                          s (.toString n base)]
                                      (if (NaN? n)
                                        ""
                                        (if @format-ratom
                                          (->> s
                                               str/reverse
                                               (re-seq re)
                                               (str/join separator)
                                               str/reverse)
                                          s)))
                             :on-change #(reset! num-ratom (js/Number.parseInt (str/replace (.. % -target -value) #"[ ,]" "") base))
                             :id html-id}]]))
               [{:base 10 :re #".{1,3}" :separator "," :title "Decimal"}
                {:base 16 :re #".{1,4}" :separator " " :title "Hexadecimal"}
                {:base 8 :re #".{1,3}" :separator " " :title "Octal"}
                {:base 2 :re #".{1,4}" :separator " " :title "Binary"}]))]]])))
