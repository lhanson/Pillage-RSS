(ns pillage.views
  (:use hiccup.core
        hiccup.page-helpers))

(defn html-doc
  "Base template for generating an HTML document"
  ([] (html-doc "Pillage RSS" [:h1 "Hello World!"]))
  ([title & body]
    (html
      (doctype :xhtml-strict)
      (xhtml-tag "en"
        [:head
          [:meta {:http-equiv "Content-type"
                  :content "text/html; charset=utf-8"}]
          [:title title]]
        [:body body]))))
