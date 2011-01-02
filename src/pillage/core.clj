(ns pillage.core
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:use compojure.core
        ring.util.servlet)
  (:require [pillage.handlers :as handlers]
            [compojure.route :as route]))

(defroutes pillage-rss
  (GET "/" {uri :uri} (handlers/home uri))

  (POST "/feeds" {uri :uri {feed_url "feed_url"} :params} (handlers/add-feed uri feed_url))

  (GET ["/feeds/:id"] {{id "id"} :params :as request} (handlers/get-feed (:uri request) id))
  (DELETE ["/feeds/:id"] {{id "id"} :params :as request} (handlers/delete-feed (:uri request) id))

  (route/not-found "<h1>Page not found</h1>"))

(defservice pillage-rss)

