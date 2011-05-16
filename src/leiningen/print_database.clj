(ns leiningen.print-database
  (:require [clojure.string :as string])
  (:use [pillage.feed-handling :only (get-syndfeed)]
        pillage.models
        [appengine.datastore :only (delete-entity save-entity)]
        appengine.environment
        [appengine.users :only (current-user)]))

(defn print-database []
  "A Leiningen task which lists the contents of the data store."
  
  (init-appengine "war")
  (with-configuration "war/WEB-INF/appengine-web.xml"
    (with-appengine (local-proxy :email "test@example.com" :admin true)
      (do
        (let [feed-count (count (find-pillagefeeds))
              exclusion-count (count (find-exclusion-filters))
              modification-count (count (find-modification-filters))
              ]
          (println "Have" feed-count "feeds, " exclusion-count "exclusion filters, and"
                   modification-count "modification filters"))))))

