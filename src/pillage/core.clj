(ns pillage.core
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:use pillage.handlers
        compojure.core
        ring.util.servlet)
  (:require [compojure.route :as route]))

(defroutes pillage-rss
  (GET "/" {uri :uri} (home uri))
  (route/not-found "<h1>Page not found</h1>"))

(defservice pillage-rss)

