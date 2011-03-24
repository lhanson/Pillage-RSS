(ns leiningen.reset-database
  (:require [clojure.string :as string])
  (:use [pillage.feed-handling :only (get-syndfeed)]
        pillage.models
        [appengine.datastore :only (delete-entity save-entity)]
        appengine.environment
        [appengine.users :only (current-user)]))

(defn reset-database []
  "A Leiningen task which wipes out the existing feed database and populates
  it with standard entries."
  
  ; The RSS feeds to populate the local database with
  (def *urls* ["http://www.sciencemag.org/rss/current.xml"
               "http://www.sciencemag.org/rss/news.xml"
               "http://www.sciencemag.org/rss/express.xml"
               "http://www.sciencemag.org/rss/twis.xml"
               "http://www.sciencemag.org/rss/ec.xml"
               "http://www.sciencemag.org/rss/podcast.xml"])
  
  (print "Delete all local feed data and populate with canned data (y/n)? ")
  (flush)
  (if (= (string/upper-case (read-line)) "Y")
    (do 
      (init-appengine "war")
      (with-configuration "war/WEB-INF/appengine-web.xml"
        (with-appengine (local-proxy :email "test@example.com" :admin true)
          (do
            (let [feed-count (count (find-pillagefeeds))
                  mod-filter-count (count (find-modification-filters))]
              ; Delete feeds
              (if (pos? feed-count)
                (do
                  (println "Deleting" feed-count "existing feeds for user "
                           (:email (current-user)))
                  (doseq [feed (find-pillagefeeds)]
                    (delete-entity feed))))
              ; Delete modification filters
              (if (pos? mod-filter-count)
                (do
                  (println "Deleting" mod-filter-count "modification filters")
                  (doseq [mod-filter (find-modification-filters)]
                    (delete-entity mod-filter)))))
            ; Create the filters
            (let [strip-images-mod (modification-filter {:author "_system_"
                                                         :label  "Strip Images From Item"
                                                         :type   "Type - TODO"
                                                         :regex  "s/image//"})
                  mod-filters [strip-images-mod]
                  mod-filter-keys (map #(:key (save-entity %1)) mod-filters)]
              ; Create the default feeds
              (doseq [url *urls*]
                (let [syndfeed (get-syndfeed url)]
                  (save-entity
                    (pillagefeed {:user-id (:email (current-user))
                                  :original-url url
                                  :feed-name (. syndfeed getTitle)
                                  :modification-filters mod-filter-keys})))))
            (println "Populated the database with" (count (find-pillagefeeds)) "feeds"))))))
  (println "Finished"))

