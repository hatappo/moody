(ns moody.tools.uuid-page
  (:require
   ["uuid" :refer [validate NIL v1 v3 v4 v5]]
   [clojure.string :as str]
   [reagent.core :as r]))

;; Too large numbers will cause the application to hang, so set an upper.
(def max-gen-count 128)

(def uuids
  [{:version :NIL :label "All zero value" :generate-fn (fn [] NIL)}
   {:version :v4 :label "UUID v4 (random)" :generate-fn v4}
   {:version :v1 :label "UUID v1 (timestamp)" :generate-fn v1}
   {:version :v3 :label "UUID v3 (namespace with MD5)" :generate-fn v3}
   {:version :v5 :label "UUID v5 (namespace with SHA-1)" :generate-fn v5}])

(def uuid-by-version (zipmap (map :version uuids) uuids))

(def uuid-namespaces
  [{:ns-kw :dns :ns-id "6ba7b810-9dad-11d1-80b4-00c04fd430c8"}
   {:ns-kw :url :ns-id "6ba7b811-9dad-11d1-80b4-00c04fd430c8"}
   {:ns-kw :oid :ns-id "6ba7b812-9dad-11d1-80b4-00c04fd430c8"}
   {:ns-kw :x500 :ns-id "6ba7b814-9dad-11d1-80b4-00c04fd430c8"}
   {:ns-kw :custom :ns-id nil}])

(def namespace-by-ns-kw (zipmap (map :ns-kw uuid-namespaces) uuid-namespaces))

(def initial-values
  {:format true
   :upper false
   :count 2
   :uuid :v4
   :name ""
   :ns-kw :dns
   :custom-ns-id ""
   :outputs ""})

(defn uuid-page
  []
  (let [format-ratom (r/atom (:format initial-values))
        upper-ratom (r/atom (:upper initial-values))
        count-ratom (r/atom (:count initial-values))
        uuid-ratom (r/atom (:uuid initial-values))
        name-ratom (r/atom (:name initial-values))
        ns-kw-ratom (r/atom (:ns-kw initial-values))
        custom-ns-id-ratom (r/atom (:custom-ns-id initial-values))
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
                                (let [gen-fn (:generate-fn (@uuid-ratom uuid-by-version))
                                      cnt (min @count-ratom max-gen-count)
                                      outputs (repeatedly
                                               cnt
                                               (if-not (#{:v3 :v5} @uuid-ratom)
                                                 gen-fn
                                                 (if-not (= @ns-kw-ratom :custom)
                                                   #(gen-fn @name-ratom (get-in namespace-by-ns-kw [@ns-kw-ratom :ns-id]))
                                                   (if (validate @custom-ns-id-ratom)
                                                     #(gen-fn @name-ratom @custom-ns-id-ratom)
                                                     (constantly (str "Invalid namespace id: " @custom-ns-id-ratom))))))]
                                  (reset! outputs-ratom outputs)))}
           "Gen"]]

         [:div {:class "flex items-center gap-2"}
          [:label {:for "count-input"}
           "Count:"]
          [:input {:class "input input-ghost-primary input-sm"
                   :type "number"
                   :id "count-input"
                   :min 1
                   :max max-gen-count
                   :value @count-ratom
                   :on-change  #(let [cnt (js/Number.parseInt (.. % -target -value))
                                      cnt (if (NaN? cnt) 1 cnt)
                                      cnt (min cnt max-gen-count)]
                                  (reset! count-ratom cnt))}]]

         [:div {:class "flex items-center gap-2"}
          [:label {:for "format-checkbox"}
           "Format:"]
          [:input {:class "switch switch-bordered-primary"
                   :type "checkbox"
                   :id "format-checkbox"
                   :checked @format-ratom
                   :on-change #(swap! format-ratom not)}]]

         [:div {:class "flex items-center gap-2"}
          [:label {:for "upper-checkbox"}
           "Upper case:"]
          [:input {:class "switch switch-bordered-primary"
                   :type "checkbox"
                   :id "upper-checkbox"
                   :checked @upper-ratom
                   :on-change #(swap! upper-ratom not)}]]]

        [:div {:class "flex flex-col gap-2"}

         ;;  FIXME: Warning: A component is changing an uncontrolled input to be controlled.
         (doall
          (map (fn [{:keys [version label]}]
                 (let [html-id (str (name version) "-radio")]
                   ^{:key version}
                   [:div {:class "flex items-center gap-2"}
                    [:input {:class "radio radio-bordered-primary"
                             :type :radio
                             :id html-id
                             :value version
                             :checked (= @uuid-ratom version)
                             :on-change #(when (= version (keyword (.. % -target -value)))
                                           (reset! uuid-ratom version))
                             :name "uuid"}]
                    [:label {:class ""
                             :for html-id}
                     label]]))
               uuids))

         (when (#{:v3 :v5} @uuid-ratom)
           [:div {:class "flex flex-col gap-2"}

            [:div {:class "flex items-center gap-2"}
             [:label {:for "name-input"}
              "Name:"]
             [:input {:class "input input-ghost-primary input-sm"
                      :type "text"
                      :id "name-input"
                      :value @name-ratom
                      :on-change  #(reset! name-ratom (.. % -target -value))}]]
            [:div {:class "flex flex-col gap-2"}
             "Namespace:"
             [:div {:class "flex flex-col gap-1 px-4"}

              [:div {:class "flex flex-row items-center gap-8"}
               "Namespace kw:"
               (doall
                (map (fn [{:keys [ns-kw]}]
                       (let [html-id (str (name ns-kw) "-ns-kw--input")]
                         ^{:key ns-kw}
                         [:div {:class "flex items-center gap-2"}
                          [:input {:class "radio radio-bordered-primary"
                                   :type :radio
                                   :name "ns-kw"
                                   :value ns-kw
                                   :checked (= @ns-kw-ratom ns-kw)
                                   :on-change #(when (= ns-kw (keyword (.. % -target -value)))
                                                 (reset! ns-kw-ratom ns-kw))
                                   :id html-id}]
                          [:label {:class ""
                                   :for html-id}
                           (name ns-kw)]]))
                     uuid-namespaces))]

              [:div {:class "flex items-center gap-2"}
               [:label {:for "ns-id-input"}
                "Namespace id:"]
               [:input {:class "input input-ghost-primary input-sm"
                        :type "text"
                        :placeholder (when (= @ns-kw-ratom :custom) "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx")
                        :read-only (not= @ns-kw-ratom :custom)
                        :id "ns-id-input"
                        :value (if (= @ns-kw-ratom :custom)
                                 @custom-ns-id-ratom
                                 (get-in namespace-by-ns-kw [@ns-kw-ratom :ns-id]))
                        :on-change #(reset! custom-ns-id-ratom (.. % -target -value))}]]]]])

         [:div {:class "flex flex-col"}
          "Generated IDs:"
          [:textarea {:class "textarea textarea-ghost-primary max-w-xl"
                      :read-only true
                      :rows 20
                      :value (-> (str/join "\n" @outputs-ratom)
                                 (cond->>  (not @format-ratom) (#(str/replace % "-" "")))
                                 (cond->> @upper-ratom  str/upper-case))
                      :on-click #(.select (.. % -target))}]]]]])))
