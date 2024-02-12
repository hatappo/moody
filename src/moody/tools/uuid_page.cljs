(ns moody.tools.uuid-page
  (:require
   ["uuid" :refer [NIL v1 v2 v4 v5]]
   [clojure.string :as str]
   [reagent.core :as r]))

;; Too large numbers will cause the application to hang, so set an upper.
(def max-gen-count 128)

(def uuids
  [{:id :v1 :label "UUID v1 (timestamp)" :generate-fn v1}
   {:id :v2 :label "UUID v2 (namespace with MD5)" :generate-fn v2}
   {:id :v4 :label "UUID v4 (random)" :generate-fn v4}
   {:id :v5 :label "UUID v5 (namespace with SHA-1)" :generate-fn v5}
   {:id :NIL :label "All zero value" :generate-fn (fn [] NIL)}])

(def uuids-by-uuid (zipmap (map :id uuids) uuids))

(def initial-values
  {:format true
   :upper false
   :count 2
   :uuid :v4
   :name ""
   :namespace ""
   :outputs ""})

(defn uuid-page
  []
  (let [format-ratom (r/atom (:format initial-values))
        upper-ratom (r/atom (:upper initial-values))
        count-ratom (r/atom (:count initial-values))
        uuid-ratom (r/atom (:uuid initial-values))
        name-ratom (r/atom (:name initial-values))
        namespace-ratom (r/atom (:namespace-ratom initial-values))
        outputs-ratom (r/atom (:outputs initial-values))]
    (fn []
      [:article {:class "flex flex-col gap-2"}
       [:h2 {:class "text-xl my-4"}
        "UUID Generator"]

       [:section {:class "flex flex-col gap-6 pr-8"}
        [:div {:class "flex flex-row gap-8"}

         [:div {:class "flex items-center gap-2"}
          [:button {:class "btn btn-solid-primary"
                    :on-click (fn [_e]
                                (let [gen-fn (:generate-fn (@uuid-ratom uuids-by-uuid))
                                      outputs (repeatedly (min @count-ratom max-gen-count) gen-fn)]
                                  (reset! outputs-ratom outputs)))}
           "Gen"]]

         [:div {:class "flex items-center gap-2"}
          [:label {:for "count-input"}
           "Count"]
          [:input {:class "input input-ghost-primary input-sm"
                   :type "number"
                   :id "count-input"
                   :min 1
                   :max max-gen-count
                   :value @count-ratom
                   :on-change  #(let [c (js/Number.parseInt (.. % -target -value))
                                      c (if (NaN? c) 1 c)
                                      c (min c max-gen-count)]
                                  (reset! count-ratom c))}]]

         [:div {:class "flex items-center gap-2"}
          [:label {:for "format-checkbox"}
           "Format"]
          [:input {:class "switch switch-bordered-primary"
                   :type "checkbox"
                   :id "format-checkbox"
                   :checked @format-ratom
                   :on-change #(swap! format-ratom not)}]]

         [:div {:class "flex items-center gap-2"}
          [:label {:for "upper-checkbox"}
           "Upper case"]
          [:input {:class "switch switch-bordered-primary"
                   :type "checkbox"
                   :id "upper-checkbox"
                   :checked @upper-ratom
                   :on-change #(swap! upper-ratom not)}]]]

        [:div {:class "flex flex-col gap-2"}

         ;;  FIXME: Warning: A component is changing an uncontrolled input to be controlled.
         (doall
          (map (fn [{:keys [id label]}]
                 (let [html-id (str (name id) "-radio")]
                   ^{:key id}
                   [:div {:class "flex items-center gap-2"}
                    [:input {:class "radio radio-bordered-primary"
                             :type :radio
                             :id html-id
                             :checked (when (= id @uuid-ratom) "on")
                             :on-change #(when (= "on" (.. % -target -value))
                                           (reset! uuid-ratom id))
                             :name "uuid"}]
                    [:label {:class ""
                             :for html-id}
                     label]]))
               uuids))

         (when (#{:v2 :v5} @uuid-ratom)
           [:div {:class "flex flex-col gap-2"}
            [:div {:class "flex items-center gap-2"}
             [:label {:for "name-input"}
              "Name"]
             [:input {:class "input input-ghost-primary input-sm"
                      :type "text"
                      :id "name-input"
                      :value @name-ratom
                      :on-change  #()}]] ; TODO:
            [:div {:class "flex items-center gap-2"}
             [:label {:for "namespace-input"}
              "Namespace"]
             [:input {:class "input input-ghost-primary input-sm"
                      :type "text"
                      :id "namespace-input"
                      :value @name-ratom
                      :on-change  #()}]]]) ; TODO:


         [:textarea {:class "textarea textarea-ghost-primary max-w-xl"
                     :read-only true
                     :rows 20
                     :value (-> (str/join "\n" @outputs-ratom)
                                (cond->>  (not @format-ratom) (#(str/replace % "-" "")))
                                (cond->> @upper-ratom  str/upper-case))
                     :on-click #(.select (.. % -target))}]]]])))
