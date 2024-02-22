(ns moody.tools.datetime-page
  (:require
   [clojure.string :as str]
   [reagent.core :as r]))

(def 干支s
  [{:kanji "申" :emoji "🐵"}
   {:kanji "酉" :emoji "🐔"}
   {:kanji "戌" :emoji "🐶"}
   {:kanji "亥" :emoji "🐗"}
   {:kanji "子" :emoji "🐭"}
   {:kanji "丑" :emoji "🐮"}
   {:kanji "寅" :emoji "🐯"}
   {:kanji "卯" :emoji "🐰"}
   {:kanji "辰" :emoji "🐲"}
   {:kanji "巳" :emoji "🐍"}
   {:kanji "午" :emoji "🐴"}
   {:kanji "未" :emoji "🐏"}])

(def 和風月名s ["睦月" "如月" "弥生" "卯月" "皐月" "水無月" "文月" "葉月" "長月" "神無月" "霜月" "師走"])

(defn date->ISO8601
  [d]
  ;; Based on https://stackoverflow.com/a/69032104/5022162
  (-> {#_#_:timeZone "Asia/Tokyo"
       :timeZoneName "longOffset"
       :year "numeric"
       :month "2-digit"
       :day "2-digit"
       :hour "2-digit"
       :minute "2-digit"
       :second "2-digit"
       :fractionalSecondDigits 3
       :hour12 false}
      clj->js
      (#(js/Intl.DateTimeFormat. "sv-SE" %)) ; `sv-SE` is `Swedish, Sweden`
      (.format d)
      (str/replace-first " " "T")
      (str/replace-first "," ".")
      (str/replace-first " GMT" "")
      (str/replace-first #":00$" "00")))

(defn date->calendar-format
  [d]
  (-> d date->ISO8601 (str/replace-first #"[-+]\d+$" "")))

(def formats
  [;; Universal
   {:id :calendar
    :label "Calendar"
    :type :datetime-local
    :format-fn date->calendar-format
    :parse-fn (fn [s] (js/Date. s))}
   {:id :iso8601
    :label "ISO 8601"
    :type :text
    :format-fn date->ISO8601
    :parse-fn (fn [s] (js/Date. s))}
   {:id :iso8601-utc
    :label "ISO 8601 (UTC)"
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

   ;;  Japanese
   {:id :era-ja-narrow
    :label "Ja year with alphabet era"
    :type :text
    :format-fn (fn [d] (first (str/split (.format (js/Intl.DateTimeFormat. "ja-JP-u-ca-japanese" #js {:era "narrow"}) d) "/")))
    :parse-fn nil}
   {:id :year-ja-short
    :label "Ja year with kanji era"
    :type :text
    :format-fn (fn [d] (.format (js/Intl.DateTimeFormat. "ja-JP-u-ca-japanese" #js {:year "numeric"}) d))
    :parse-fn nil}
   {:id :year-ja-eto-kanji
    :label "Ja zodiac (kanji)"
    :type :text
    :format-fn (fn [d] (->> d (.getFullYear) (#(mod % 12)) (nth 干支s) :kanji))
    :parse-fn nil}
   {:id :year-ja-eto-emoji
    :label "Ja zodiac (emoji)"
    :type :text
    :format-fn (fn [d] (->> d (.getFullYear) (#(mod % 12)) (nth 干支s) :emoji))
    :parse-fn nil}
   {:id :month-ja-kanji
    :label "Ja traditional month"
    :type :text
    :format-fn (fn [d] (->> d (.getMonth) (nth 和風月名s)))
    :parse-fn nil}
   {:id :weekday-ja-long
    :label "Ja weekday"
    :type :text
    :format-fn (fn [d] (.format (js/Intl.DateTimeFormat. "ja-JP" #js {:weekday "long"}) d))
    :parse-fn nil}

   ;; English / Arabian
   {:id :year-number-full
    :label "Year"
    :type :text
    :format-fn (fn [d] (.getFullYear d))
    :parse-fn nil}
   {:id :month-en-numeric
    :label "Month (numeric)"
    :type :text
    :format-fn (fn [d] (.format (js/Intl.DateTimeFormat. "en-US" #js {:month "numeric"}) d))
    :parse-fn nil}
   {:id :month-en-full
    :label "Month (long)"
    :type :text
    :format-fn (fn [d] (.format (js/Intl.DateTimeFormat. "en-US" #js {:month "long"}) d))
    :parse-fn nil}
   {:id :month-en-short
    :label "Month (short)"
    :type :text
    :format-fn (fn [d] (.format (js/Intl.DateTimeFormat. "en-US" #js {:month "short"}) d))
    :parse-fn nil}
   {:id :day-en-numeric
    :label "Day"
    :type :text
    :format-fn (fn [d] (.format (js/Intl.DateTimeFormat. "en-US" #js {:day "numeric"}) d))
    :parse-fn nil}
   {:id :weekday-en-long
    :label "Weekday (long)"
    :type :text
    :format-fn (fn [d] (.format (js/Intl.DateTimeFormat. "en-US" #js {:weekday "long"}) d))
    :parse-fn nil}
   {:id :weekday-en-short
    :label "Weekday (short)"
    :type :text
    :format-fn (fn [d] (.format (js/Intl.DateTimeFormat. "en-US" #js {:weekday "short"}) d))
    :parse-fn nil}
   {:id :hour-en-am-pm
    :label "Hours (12)"
    :type :text
    :format-fn (fn [d] (.format (js/Intl.DateTimeFormat. "en-US" #js {:hour "numeric"}) d))
    :parse-fn nil}
   {:id :hours-en-24
    :label "Hours (24)"
    :type :text
    :format-fn (fn [d] (->> d (.getHours)))
    :parse-fn nil}
   {:id :minutes-en
    :label "Minutes"
    :type :text
    :format-fn (fn [d] (->> d (.getMinutes)))
    :parse-fn nil}
   {:id :seconds-en
    :label "Seconds"
    :type :text
    :format-fn (fn [d] (->> d (.getSeconds)))
    :parse-fn nil}

   {:id :milliseconds-en
    :label "Milliseconds"
    :type :text
    :format-fn (fn [d] (->> d (.getMilliseconds)))
    :parse-fn nil}])

(def initial-values
  {:d (js/Date.)
   :num 1
   :unit :day})

(defn datetime-page
  []
  (let [d-ratom (r/atom (:d initial-values))
        num-ratom (r/atom (:num initial-values))
        unit-ratom (r/atom (:unit initial-values))]
    (fn []
      [:article {:class "flex flex-col gap-2"}
       [:h2 {:class "text-xl my-4"}
        "Datetime Converter"]

       [:div {:class "flex flex-wrap gap-20"}

        [:section {:class "flex flex-col gap-8 pr-8"}

         [:div {:class "flex flex-col gap-1"}
          [:div {:class "btn-group btn-group-scrollable"}
           [:button {:class "btn btn-solid-primary"
                     :on-click #(reset! d-ratom (let [d (js/Date.)]
                                                  (.setUTCDate d (dec (.getUTCDate d)))
                                                  (-> #js {:year "numeric" :month "2-digit" :day "2-digit"}
                                                      js/Intl.DateTimeFormat.
                                                      (.format d)
                                                      js/Date.)))}
            "Yesterday"]
           [:button {:class "btn btn-solid-primary"
                     :on-click #(reset! d-ratom (-> #js {:year "numeric" :month "2-digit" :day "2-digit"}
                                                    js/Intl.DateTimeFormat.
                                                    (.format (js/Date.))
                                                    js/Date.))}
            "Today"]
           [:button {:class "btn btn-solid-primary"
                     :on-click #(reset! d-ratom (js/Date.))}
            "Now"]
           [:button {:class "btn btn-solid-primary"
                     :on-click #(reset! d-ratom (let [d (js/Date.)]
                                                  (.setUTCDate d (inc (.getUTCDate d)))
                                                  (-> #js {:year "numeric" :month "2-digit" :day "2-digit"}
                                                      js/Intl.DateTimeFormat.
                                                      (.format d)
                                                      js/Date.)))}
            "Tomorrow"]]

          [:div {:class "btn-group btn-group-scrollable"}
           [:button {:class "btn btn-solid-primary"
                     :on-click (fn [_e]
                                 (let [d @d-ratom
                                       num (js/Number.parseInt @num-ratom)]
                                   (case (keyword @unit-ratom)
                                     :sec (do (.setSeconds d (+ (.getSeconds d) num))
                                              (reset! d-ratom (js/Date. (.getTime d))))
                                     :min (do (.setMinutes d (+ (.getMinutes d) num))
                                              (reset! d-ratom (js/Date. (.getTime d))))
                                     :hour (do (.setHours d (+ (.getHours d) num))
                                               (reset! d-ratom (js/Date. (.getTime d))))
                                     :day (do (.setDate d (+ (.getDate d) num))
                                              (reset! d-ratom (js/Date. (.getTime d))))
                                     :month (do (.setMonth d (+ (.getMonth d) num))
                                                (reset! d-ratom (js/Date. (.getTime d))))
                                     :year (do (.setFullYear d (+ (.getFullYear d) num))
                                               (reset! d-ratom (js/Date. (.getTime d)))))))}
            "Add"]
           [:input {:class "input input-ghost-primary w-36"
                    :type "number"
                    :value @num-ratom
                    :on-change #(reset! num-ratom (.. % -target -value))
                    :max 100000
                    :min -100000}]
           [:select {:class "select select-ghost-primary w-32"
                     :value @unit-ratom
                     :on-change #(reset! unit-ratom (.. % -target -value))}
            [:option {:value :sec} "second(s)"]
            [:option {:value :min} "minute(s)"]
            [:option {:value :hour} "hour(s)"]
            [:option {:value :day} "day(s)"]
            [:option {:value :month} "month(s)"]
            [:option {:value :year} "year(s)"]]]]

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

        [:section {:class "flex flex-col pr-8"}
         [:div {:class "flex flex-col gap-2"}
          (doall
           (->> formats
                (remove #(:parse-fn %))
                (remove #(str/includes? (name (:id %)) "-ja-"))
                (map (fn [{:keys [id label type format-fn]}]
                       (let [html-id (str "datetime-" (name id) "-input")]
                         ^{:key id}
                         [:div {:class "flex justify-evenly items-center gap-1"}
                          [:label {:class "w-36"
                                   :for html-id} label]
                          [:input {:class "input input-sm input-block input-ghost-primary input-solid w-36"
                                   :type type
                                   :read-only true
                                   :value (when-not (and @d-ratom (NaN? @d-ratom))
                                            (format-fn @d-ratom))
                                   :on-click #(.select (.. % -target))
                                   :id html-id}]])))))]

         [:div {:class "divider divider-horizontal"}]

         [:div {:class "flex flex-col gap-2"}
          (doall
           (->> formats
                (remove #(:parse-fn %))
                (filter #(str/includes? (name (:id %)) "-ja-"))
                (map (fn [{:keys [id label type format-fn]}]
                       (let [html-id (str "datetime-" (name id) "-input")]
                         ^{:key id}
                         [:div {:class "flex justify-evenly items-center gap-1"}
                          [:label {:class "w-48"
                                   :for html-id} label]
                          [:input {:class "input input-sm input-block input-ghost-primary input-solid w-24"
                                   :type type
                                   :read-only true
                                   :value (when-not (and @d-ratom (NaN? @d-ratom))
                                            (format-fn @d-ratom))
                                   :on-click #(.select (.. % -target))
                                   :id html-id}]])))))]]]])))
