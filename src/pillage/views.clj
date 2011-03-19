(ns pillage.views
  (:use hiccup.core
        hiccup.page-helpers
        hiccup.form-helpers
        [appengine.datastore :only (key->string)]))

(def default-title "Pillage RSS")

(def banner
  '([:div.center
      [:h1 "Pillage!"
       [:a {:href "/"} [:img {:src "/images/rss-symbol.jpg"}]]]]))

(defn- default-body-unauthenticated [login-url]
  `([:h1 "Hi there!"]
    [:p [:a {:href ~login-url} "Log in"] " using your Google account."]))

(defn- display-feed [{id :key
                      feed-name :feed-name
                      pillaged-url :pillaged-url
                      original-url :original-url}]
  [:p
    [:a {:href (str "/feeds/" (key->string id))} feed-name] "|"
    [:a {:href original-url} "original"]
    (form-to [:delete (str "/feeds/" (key->string id))] (submit-button "delete"))])

(defn- printfeed [{:keys [key, feed-name, user-id]}]
  (println "Feed key:" (key->string key) ", title:" feed-name ", user id:" user-id))

(defn- radio [group-name value label-text default-checked value-map]
  "Generates a hiccup.form-helpers/radio-button (and associated label) with
   its checked state determined by the associated value in value-map.
  
   Example: (radio \"num_items\" \"number of items\" true {:num_items \"one\"} \"none\")
     would result in the radio button being unchecked because the value-map
     is populated and does not match the value."
  (radio-button
    (str group-name)
    ; Check it if the value-map has this value set for group-name
    ; or if no value is set and the default is true
    (let [value-from-map ((keyword group-name) value-map)]
      (or (= value value-from-map)
          (and (nil? value-from-map) default-checked)))
    value))

(defn- default-body [username logout-url feeds]
  `([:p "You're logged in, " ~username "."]
    [:p [:a {:href ~logout-url} "Log out"]]
    [:div
      [:h1 "Feeds"]
      ~(if (empty? feeds) "You don't have any feeds yet, add some!")
      ~(form-to [:post "/feeds"]
        (text-field "feed_url")
        (submit-button "Add"))
      ~(map printfeed feeds)
      ~(map display-feed feeds)]))

(defn- edit-body [username logout-url feed filters]
  (println "Editing feed" feed "and filters" filters)
  (let [{:keys [key original-url feed-name]} feed]
    `([:p "You're logged in, " ~username "."]
      [:p [:a {:href logout-url} "Log out"]]
      [:div
        [:h1 "Edit Feed - " ~feed-name]
        [:p "Source feed: "
          [:a {:href ~original-url} ~original-url]]
        ~(form-to [:post (str "/feeds/" (key->string key))]
          [:p "Feed name "
           [:input {:id "name" :name "name" :type "text"
                    :size (count feed-name) :value feed-name}]]

          [:h2 "Strip entire items from the feed"]
          (let [{:keys [exclusions modifications]} filters]
            `([:p "Have " ~(count exclusions) " exclusion filters"]
              [:p "Have " ~(count modifications) " modification filters"]))
;          (radio "strip-items" "none" "keep all items" true transformation)
;          (label "none" "keep all items")
;          (radio "strip-items" "remove-image-only-items" "remove image-only items" false transformation)
;          (label "none" "remove image-only items")
;          (radio "strip-items" "remove-text-only" "remove text-only items" false transformation)
;          (label "none" "remove text-only items")
;
;          [:h2 "Alter individual feed items"]
;          [:p "TODO"]
          (submit-button "Update"))])))

(defn html-doc
  "Base template for generating an HTML document"
  ([] (html-doc default-title {:content default-body-unauthenticated} ))
  ([body] (html-doc default-title body))
  ([title & [{content :content} body]]
    (html
      (doctype :xhtml-strict)
      (xhtml-tag "en"
        [:head (include-css "/css/layout.css")
          [:meta {:http-equiv "Content-type"
                  :content "text/html; charset=utf-8"}]
          [:title title]]
        [:body 
          [:div#container
            [:div#banner banner]
            ;[:div#banner ;[:a {:href "http://google.com"} [:img {:src "http://www.intensivstation.ch/files/images/monorom_css_logo.gif"}]]
                         ;[:h1 "Header"]]
            [:div#outer ;TODO: currently redundant
              [:div#inner
                [:div#content content]]]
            [:div#footer ]]]))))
            ;[:div#footer [:h1 "Footer"]]]]))))

(defn need-to-login [login-url]
  (html-doc {:content (default-body-unauthenticated login-url)}))

(defn home [username logout-url feeds]
  (html-doc {:content (default-body username logout-url feeds)}))

(defn edit [username logout-url feed filters]
  (html-doc {:content (edit-body username logout-url feed filters)}))

