(ns moody.tools.datetime-page
  (:require
   [clojure.string :as str]
   [reagent.core :as r]))

(def 和風月名s ["睦月" "如月" "弥生" "卯月" "皐月" "水無月" "文月" "葉月" "長月" "神無月" "霜月" "師走"])

(def formats
  [{:id :calendar
    :label "Calendar"
    :type :datetime-local
    :format-fn (fn [d] (str/replace (.toISOString d) "Z" ""))
    :parse-fn (fn [s] (js/Date. s))}
   {:id :iso8601
    :label "ISO 8601"
    :type :text
    :format-fn (fn [d] (.toISOString d))
    :parse-fn (fn [s] (js/Date. s))}
   {:id :ts-ms
    :label "Timestamp (Milliseconds)"
    :type :text
    :format-fn #(.getTime %)
    :parse-fn (fn [s] (js/Date. (js/Number.parseInt s)))}
   {:id :ts-sec
    :label "UNIX Timestamp (Seconds)"
    :type :text
    :format-fn (fn [d] (.toString (js/Math.floor (/ (.getTime d) 1000))))
    :parse-fn (fn [s] (js/Date. (* 1000 (js/Number.parseInt s))))}



   {:id :era-ja-short
    :label "元号 short"
    :type :text
    :format-fn (fn [d] (.format (js/Intl.DateTimeFormat. "ja-JP-u-ca-japanese" #js {:era "short"}) d))
    :parse-fn nil}
   {:id :era-ja-narrow
    :label "元号 narrow"
    :type :text
    :format-fn (fn [d] (.format (js/Intl.DateTimeFormat. "ja-JP-u-ca-japanese" #js {:era "narrow"}) d))
    :parse-fn nil}
   {:id :year-number-full
    :label "Year"
    :type :text
    :format-fn (fn [d] (.getFullYear d))
    :parse-fn nil}
   {:id :month-en-numeric
    :label "Month en numeric"
    :type :text
    :format-fn (fn [d] (.format (js/Intl.DateTimeFormat. "en-US" #js {:month "numeric"}) d))
    :parse-fn nil}
   {:id :month-en-full
    :label "Month en long"
    :type :text
    :format-fn (fn [d] (.format (js/Intl.DateTimeFormat. "en-US" #js {:month "long"}) d))
    :parse-fn nil}
   {:id :month-en-short
    :label "Month en short"
    :type :text
    :format-fn (fn [d] (.format (js/Intl.DateTimeFormat. "en-US" #js {:month "short"}) d))
    :parse-fn nil}
   {:id :month-ja-kanji
    :label "Month 和風月名"
    :type :text
    :format-fn (fn [d] (->> d (.getMonth) (nth 和風月名s)))
    :parse-fn nil}
   {:id :day-en-numeric
    :label "Day en numeric"
    :type :text
    :format-fn (fn [d] (.format (js/Intl.DateTimeFormat. "en-US" #js {:day "numeric"}) d))
    :parse-fn nil}
   {:id :weekday-en-long
    :label "Weekday en long"
    :type :text
    :format-fn (fn [d] (.format (js/Intl.DateTimeFormat. "en-US" #js {:weekday "long"}) d))
    :parse-fn nil}
   {:id :weekday-en-short
    :label "Weekday en short"
    :type :text
    :format-fn (fn [d] (.format (js/Intl.DateTimeFormat. "en-US" #js {:weekday "short"}) d))
    :parse-fn nil}
   {:id :weekday-ja-long
    :label "Weekday ja"
    :type :text
    :format-fn (fn [d] (.format (js/Intl.DateTimeFormat. "ja-JP" #js {:weekday "long"}) d))
    :parse-fn nil}
   {:id :hour-en-am-pm
    :label "Hour en 12 AM/PM"
    :type :text
    :format-fn (fn [d] (.format (js/Intl.DateTimeFormat. "en-US" #js {:hour "numeric"}) d))
    :parse-fn nil}
   {:id :hours-en-24
    :label "Hours en 24"
    :type :text
    :format-fn (fn [d] (->> d (.getHours)))
    :parse-fn nil}
   {:id :minutes-en
    :label "Minutes en"
    :type :text
    :format-fn (fn [d] (->> d (.getMinutes)))
    :parse-fn nil}
   {:id :seconds-en
    :label "Seconds en"
    :type :text
    :format-fn (fn [d] (->> d (.getSeconds)))
    :parse-fn nil}
   {:id :milliseconds-en
    :label "Milliseconds en"
    :type :text
    :format-fn (fn [d] (->> d (.getMilliseconds)))
    :parse-fn nil}])

(def initial-values
  {:d (js/Date.)})

(defn datetime-page
  []
  (let [d-ratom (r/atom (:d initial-values))]
    (fn []
      [:article {:class "flex flex-col gap-2"}
       [:h2 {:class "text-xl my-4"}
        "Datetime Converter"]

       [:div {:class "flex flex-wrap gap-20"}

        [:section {:class "flex flex-col gap-8 pr-8"}

         [:div {:class "flex flex-col gap-2 pr-8"}
          [:div {:class "flex flex-col gap-2 pr-8"}
           [:div {:class "flex items-center gap-2"}
            [:button {:class "btn btn-solid-primary"
                      :on-click #(reset! d-ratom (js/Date.))}
             "Now"]]]]

         [:div {:class "flex flex-col gap-4"}
          (doall
           (->> formats
                (filter #(:parse-fn %))
                (map (fn [{:keys [id label type format-fn parse-fn]}]
                       (let [html-id (str "datetime-" (name id) "-input")]
                         ^{:key id}
                         [:div {:class "flex flex-col gap-1"}
                          [:label {:for html-id} label]
                          [:input {:class "input input-sm input-block input-ghost-primary"
                                   :type type
                                   :value (when-not (and @d-ratom (NaN? @d-ratom))
                                            (format-fn @d-ratom))
                                   :on-change #(reset! d-ratom (parse-fn (.. % -target -value)))
                                   :id html-id}]])))))]]

        [:section {:class "flex flex-col gap-8 pr-8"}
         [:div {:class "flex flex-col gap-4"}
          (doall
           (->> formats
                (remove #(:parse-fn %))
                (map (fn [{:keys [id label type format-fn]}]
                       (let [html-id (str "datetime-" (name id) "-input")]
                         ^{:key id}
                         [:div {:class "flex justify-evenly items-center gap-1"}
                          [:label {:class "w-64"
                                   :for html-id} label]
                          [:input {:class "input input-sm input-block input-ghost-primary input-solid"
                                   :type type
                                   :read-only true
                                   :value (when-not (and @d-ratom (NaN? @d-ratom))
                                            (format-fn @d-ratom))
                                   :on-click #(.select (.. % -target))
                                   :id html-id}]])))))]]]])))
