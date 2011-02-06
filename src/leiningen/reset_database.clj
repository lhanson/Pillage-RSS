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
  (def urls ["http://www.sciencemag.org/rss/current.xml"
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
                  transformation-count (count (find-feed-transformations))]
              (if (not (= feed-count transformation-count))
                (println "!!! Feed count (" feed-count ") doesn't match "
                         "transformation count(" transformation-count ")"))
              (if (pos? feed-count)
                (do
                  (println "Deleting" feed-count "existing feeds for user "
                           (:email (current-user)))
                  (doseq [feed (find-pillagefeeds)]
                    (delete-entity feed))
                  (if-let [transformations (find-feed-transformations)]
                    (do
                      (println "!!! Deleting" (count transformations)
                               "orphaned transformations")
                      (doseq [transformation transformations]
                        (println "! Deleting transformation" transformation)
                        (delete-entity transformation)))))))
            (doseq [url urls]
              (let [syndfeed (get-syndfeed url)]
                (save-entity (pillagefeed {:user-id (:email (current-user))
                                           :original-url url
                                           :pillaged-feed "http://pillage.appspot.com/feeds/a3kdkfjjbjbj"
                                           :feed-name (. syndfeed getTitle)}))))
            (println "Populated the database with" (count (find-pillagefeeds)) "feeds")))))))

