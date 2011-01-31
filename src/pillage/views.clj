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
    [:a {:href pillaged-url} feed-name] "|"
    [:a {:href (str "/feeds/" (key->string id))} "edit"] "|"
    (form-to [:delete (str "/feeds/" (key->string id))] (submit-button "delete"))
    [:a {:href original-url} "original"]])

(defn- printfeed [{:keys [key, feed-name, user-id]}]
  (println "Feed key:" (key->string key) ", title:" feed-name ", user id:" user-id))

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

(defn- edit-body [username logout-url feed]
  (println "Editing feed" feed)
  (let [{:keys [feed-name original-url]} feed]
    `([:p "You're logged in, " ~username "."]
      [:p [:a {:href logout-url} "Log out"]]
      [:div
        [:h1 "Edit Feed - " ~feed-name]
        [:p "Source feed: "
          [:a {:href original-url} ~original-url]]
        ~(form-to [:post "/feeds"]
          [:p "Feed name " (text-field "name" feed-name)]
          [:h2 "Strip entire items from the feed"]
          (radio-button "strip_items" true "none")
          (label "none" "keep all items")
          (radio-button "strip_items" false "remove-image-only-items")
          (label "none" "remove image-only items")
          (radio-button "strip_items" false "remove-text-only")
          (label "none" "remove text-only items")
          [:h2 "Alter individual feed items"]
          [:p "TODO"]
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

(defn edit [username logout-url feed]
  (let [{:keys [feed-name]} feed]
    (html-doc {:content (edit-body username logout-url feed)})))

