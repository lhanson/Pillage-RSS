(ns pillage.views
  (:use hiccup.core
        hiccup.page-helpers))

(def html-header
  '([:h1 "Pillage!"
    [:img {:src "/images/rss-symbol.jpg"}]]))

(def body-unauthenticated
  '([:h1 "Log in, bro."]))

(def body
  '([:p "You're logged in, bro."]))

(defn html-doc
  "Base template for generating an HTML document"
  ([] (html-doc "Pillage RSS" {:banner html-header :content body} ))
  ([title & [{banner :banner content :content} body]]
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

