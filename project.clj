(defproject pillage-rss "1.0.0-SNAPSHOT"
  :description "An RSS filter which allows you to strip uninteresting content from your feeds."
  :url "http://pillage-rss.appengine.com"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]]
  :aot []
  :compile-path "war/WEB-INF/classes"
  :library-path "war/WEB-INF/lib")
