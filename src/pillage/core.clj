(ns pillage.core
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:use compojure.core
        ring.util.servlet
        ring.util.response)
  (:require [pillage.handlers :as handlers]
            [pillage.models :as models]
            [compojure.route :as route]))

(defn- keys-to-keywords [m]
  "Returns a map for which the keys in m have been transformed to keywords"
  (into {} (for [[k v] m] [(keyword k) v])))

(defroutes pillage-rss
  (GET "/" {uri :uri} (handlers/home uri))

  (GET "/feeds" {uri :uri} (handlers/home uri))
  (POST "/feeds" {uri :uri {feed_url "feed_url"} :params}
    (handlers/add-feed uri feed_url))

  (GET ["/feeds/:id"] {{id "id"} :params :as request}
    (handlers/edit-feed (:uri request) id))
  (GET ["/feeds/:id.rss"] {{id "id"} :params :as request}
    (handlers/get-feed (:uri request) id))
  (DELETE ["/feeds/:id"] {{id "id"} :params :as request}
    (handlers/delete-feed (:uri request) id))
  (POST "/feeds/:id" {uri :uri form-params :form-params {id "id"} :params}
    (handlers/update-feed uri id (keys-to-keywords form-params))
    (redirect (str "/feeds/" id)))

  (route/not-found "<h1>Page not found</h1>"))

(defservice pillage-rss)

