(ns moody.tools.hash-page
  (:require
   ["crypto-js" :as crypto-js]
   ["crypto-js/md5" :as crypto-js-md5]
   ["crypto-js/ripemd160" :as crypto-js-ripemd160]
   ["crypto-js/sha1" :as crypto-js-sha1]
   ["crypto-js/sha224" :as crypto-js-sha224]
   ["crypto-js/sha256" :as crypto-js-sha256]
   ["crypto-js/sha3" :as crypto-js-sha3]
   ["crypto-js/sha384" :as crypto-js-sha384]
   ["crypto-js/sha512" :as crypto-js-sha512]
   ["filepond-plugin-image-preview" :as FilePondPluginImagePreview]
   ["react-filepond" :refer [FilePond registerPlugin]]
   ["react-icons/bs" :as icons-bs]
   [clojure.string :as str]
   [moody.components.button :refer [copy-button paste-button]]
   [reagent.core :as r]
   [taoensso.timbre :as timbre]))

(def initial-hashes
  [{:id "MD5" :digest-fn crypto-js-md5 :digest (.toString (crypto-js-md5 ""))}
   {:id "SHA1" :digest-fn crypto-js-sha1 :digest (.toString (crypto-js-sha1 ""))}
   {:id "SHA224" :digest-fn crypto-js-sha224 :digest (.toString (crypto-js-sha224 ""))}
   {:id "SHA256" :digest-fn crypto-js-sha256 :digest (.toString (crypto-js-sha256 ""))}
   {:id "SHA384" :digest-fn crypto-js-sha384 :digest (.toString (crypto-js-sha384 ""))}
   {:id "SHA512" :digest-fn crypto-js-sha512 :digest (.toString (crypto-js-sha512 ""))}
   {:id "SHA3-512" :digest-fn crypto-js-sha3 :digest (.toString (crypto-js-sha3 ""))}
   {:id "RIPEMD160" :digest-fn crypto-js-ripemd160 :digest (.toString (crypto-js-ripemd160 ""))}])

(defn update-digests [hashes message]
  (map (fn [{:keys [digest-fn] :as hash}]
         (->> message digest-fn .toString (assoc hash :digest)))
       hashes))

(def initial-values
  {:message ""
   :input ""
   :checksum ""
   :hashes initial-hashes
   :upper true})

(defn hash-page
  []
  (let [message-ratom (r/atom (:message initial-values))
        input-ratom (atom (:input initial-values))
        checksum-ratom (r/atom (:checksum initial-values))
        hashes-ratom (r/atom (:hashes initial-values))
        upper-ratom (r/atom (:upper initial-values))]
    (fn []
      (let [checksum-matched-digest-ids-set
            (->> @hashes-ratom
                 (map (fn [{id :id digest :digest}]
                        (when (and (some? digest)
                                   (= (str/lower-case (str @checksum-ratom))
                                      (str/lower-case (str digest))))
                          id)))
                 (remove nil?)
                 set)

            exist-checksum-matched-digest?
            (pos? (count checksum-matched-digest-ids-set))]

        [:article {:class "flex flex-col gap-2"}
         [:h2 {:class "text-xl my-4"}
          "Hashing Algorithm & Checksum"]

         [:section {:class "flex flex-col gap-6 pr-8"}

          [:div {:class "flex items-end gap-8"}
           [:div {:class "flex flex-col gap-1"}
            [:div {:class "flex items-center gap-1 "}
             [:span {:class "mr-auto"}
              "Input message:"]
             (copy-button @message-ratom)
             (paste-button (fn [text]
                             (set! (.-value @input-ratom) text)
                             (reset! message-ratom text)))]
            [:textarea {:class "textarea textarea-ghost-primary"
                        :cols 33
                        :rows 8
                        #_#_:value @message-ratom
                        :default-value @message-ratom
                        :ref (fn [element]
                               (timbre/info "ref is called" element)
                               (when element (reset! input-ratom element)))
                        :on-change (fn [e]
                                     (reset! message-ratom (.. e -target -value))
                                     (reset! hashes-ratom (update-digests @hashes-ratom @message-ratom)))}]]

           [:div {:class "flex flex-col gap-12"}
            [:div {:class "flex gap-4"}
             [:div {:class "flex items-center gap-2"}
              [:label {:for "upper-checkbox"}
               "Upper case:"]
              [:input {:class "switch switch-bordered-primary mr-2"
                       :type "checkbox"
                       :id "upper-checkbox"
                       :checked @upper-ratom
                       :on-change (fn [_e] (swap! upper-ratom not))}]]]

            [:div {:class "flex flex-col gap-1"}
             [:link {:rel "stylesheet" :type "text/css" :href "/css/filepond.min.css"}]
             [:link {:rel "stylesheet" :type "text/css" :href "/css/filepond-plugin-image-preview.min.css"}]
             (registerPlugin FilePondPluginImagePreview)
             [:label {:class ""}
              "Input message from a file:"]
             [:> FilePond  {:oninitfile #(timbre/info :oninitfile %)
                            :onaddfilestart #(timbre/info :onaddfilestart %)
                            :onaddfile (fn [err file]
                                         (timbre/info :onaddfile err file)
                                         (when-not err
                                           (let [reader (js/FileReader.)]
                                             (set! (.-onload reader)
                                                   (fn [e]
                                                     (let [result (.. e -target -result)
                                                           word-array (.create (.. crypto-js -lib -WordArray) result)]
                                                       (reset! message-ratom "")
                                                       (set! (.-value @input-ratom) "")
                                                       (reset! hashes-ratom (update-digests @hashes-ratom word-array)))))
                                             (.readAsArrayBuffer reader (.-file file)))))
                            :onremovefile (fn [err file]
                                            (timbre/info :onremovefile err file)
                                            (reset! message-ratom "")
                                            (set! (.-value @input-ratom) "")
                                            (reset! hashes-ratom (update-digests @hashes-ratom "")))
                            :onerror #(timbre/warn :onerror %)
                            :maxFiles 1
                            :name "files"
                            :className "w-96"
                            #_#_:credits {:label "" :url ""}
                            :stylePanelAspectRatio "0.25"
                            :labelIdle "Drag & Drop a file or <span class=\" filepond--label-action \">Browse</span>, which you want to calculate the message digest"}]]]]



          [:div {:class "flex flex-col gap-2 text-sm"}
           [:span
            [:label {:class "flex items-center"
                     :for "checksum-input"}
             "Fill checksum if you want to confirm match:"
             (when exist-checksum-matched-digest?
               [:<>
                [:span {:class "ml-8 mr-2"}
                 [:> icons-bs/BsCheckCircle {:class "text-success" :size "1.3em"}]]
                [:span {:class "text-success"}
                 "Matched with "
                 (str/join ", " checksum-matched-digest-ids-set)]])]]
           [:div {:class "flex gap-1"}
            (paste-button (fn [text] (reset! checksum-ratom text)) "")
            [:input {:class (str (comment :class) "input input-sm input-block text-xs"
                                 (if exist-checksum-matched-digest?
                                   " input-ghost-success text-success"
                                   " input-ghost-primary"))
                     :type "text"
                     :value @checksum-ratom
                     :on-click #(.select (.. % -target))
                     :on-change (fn [e] (reset! checksum-ratom (.. e -target -value)))
                     :id "checksum-input"}]]]

          [:div {:class "flex flex-col gap-2 text-sm"}
           (doall
            (map (fn [{:keys [id digest]}]
                   (let [digest (cond->> digest @upper-ratom str/upper-case)
                         match-checksum? (= (str/lower-case @checksum-ratom) (str/lower-case digest))]
                     ^{:key id}
                     [:div {:class "flex flex-col gap-1"}
                      [:div {:class "flex items-center gap-2"}
                       [:label {:class (when match-checksum? (comment :class) "text-success")
                                :for id}
                        id]
                       (when match-checksum? [:> icons-bs/BsCheckCircle {:class "text-success" :size "1.3em"}])]
                      [:div {:class "flex gap-1"}
                       (copy-button digest "")
                       [:input {:class (str (comment :class) "input input-solid input-sm input-block text-xs"
                                            (when match-checksum? (comment :class) " text-success"))
                                :type "text"
                                :value digest
                                :on-click #(.select (.. % -target))
                                :read-only true
                                :id id}]]]))
                 @hashes-ratom))]]]))))
