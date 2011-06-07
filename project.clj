(defproject pillage-rss "1.0.0-SNAPSHOT"
  :description "An RSS filter which allows you to strip uninteresting content from your feeds."
  :url "http://pillage-rss.appengine.com"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [compojure "0.5.2"]
                 [hiccup "0.3.0"]
                 [appengine "0.4.3-SNAPSHOT"]
                 [rome "0.9"]
                 ; Including xerces JAR to sidestep "JDOM could not create a SAX parser"
                 ; issue per http://code.google.com/p/googleappengine/issues/detail?id=1367
                 [org.bluestemsoftware.open.maven.tparty/xerces-impl "2.9.0"]]
  :dev-dependencies [[com.google.appengine/appengine-api-labs "1.5.0"]
                     [com.google.appengine/appengine-api-stubs "1.5.0"]
                     [com.google.appengine/appengine-testing "1.5.0"]
                     [rome "0.9"]
                     [org.clojars.lhanson/appengine "0.4.4-SNAPSHOT"]
                     [robert/hooke "1.1.0"]]
  :aot [pillage.core]
  :compile-path "war/WEB-INF/classes"
  :library-path "war/WEB-INF/lib"
  :test-selectors { :default (fn [_] true)
                    :add-feed :add-feed})

