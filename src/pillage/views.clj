(ns pillage.views
  (:use hiccup.core
        hiccup.page-helpers))

(def default-title "Pillage RSS")

(def banner
  '([:h1 "Pillage!"
    [:a {:href "/"} [:img {:src "/images/rss-symbol.jpg"}]]]))

(defn- default-body-unauthenticated [login-url]
  `([:h1 "Hi there!"]
    [:p [:a {:href ~login-url} "Log in"] " using your Google account."]))

(defn- default-body [username logout-url]
  `([:p "You're logged in, " ~username "."]
    [:p [:a {:href ~logout-url} "Log out"]]))

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
            [:div#outer
              [:div#inner
                [:div#left "Left"]
                [:div#content content]]]
            [:div#footer ]]]))))
            ;[:div#footer [:h1 "Footer"]]]]))))

(defn need-to-login [login-url]
  (html-doc {:content (default-body-unauthenticated login-url)}))

(defn logged-in [username logout-url]
  (html-doc {:content (default-body username logout-url)}))
