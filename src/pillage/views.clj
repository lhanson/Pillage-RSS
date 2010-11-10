(ns pillage.views
  (:use hiccup.core
        hiccup.page-helpers))

(def html-header
  '([:h1 "Pillage!"
    [:img {:src "/images/rss-symbol.jpg"}]]))

(defn html-doc
  "Base template for generating an HTML document"
  ([] (html-doc "Pillage RSS" html-header ))
  ([title & body]
    (html
      (doctype :xhtml-strict)
      (xhtml-tag "en"
        [:head (include-css "/css/layout.css")
          [:meta {:http-equiv "Content-type"
                  :content "text/html; charset=utf-8"}]
          [:title title]]
        [:body body]))))

