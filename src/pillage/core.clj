(ns pillage.core
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:use compojure.core
        ring.util.servlet)
  (:require [pillage.handlers :as handlers]
            [compojure.route :as route]))

(defroutes pillage-rss
  (GET "/" {uri :uri} (handlers/home uri))
  (POST "/feeds" {uri :uri params :params} (handlers/add-feed uri (params "feed_url")))
  (DELETE ["/feeds/:id"] [id] (handlers/delete-feed id))
  (route/not-found "<h1>Page not found</h1>"))

(defservice pillage-rss)

