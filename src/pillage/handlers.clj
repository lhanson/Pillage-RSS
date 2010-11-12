(ns pillage.handlers
  (:use pillage.views)
  (:use [appengine.users :only (current-user)]))

(defn home []
  "Default request handler"
  (println "User: " (current-user))
  (html-doc))
