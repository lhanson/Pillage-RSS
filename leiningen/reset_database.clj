(ns leiningen.reset-database
  (:require [clojure.string :as string])
  (:use [pillage.feed-handling :only (get-syndfeed)]
        pillage.models
        [appengine.datastore :only (delete-entity save-entity)]
        appengine.environment
        [appengine.users :only (current-user)]))

;;;; A Leiningen task which wipes out the existing feed database and populates
;;;; it with standard entries.

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
          (let [feed-count (count (find-feeds))]
            (if (pos? feed-count)
              (do
                (println "Deleting " feed-count " existing feeds for user "
                         (:email (current-user)))
                (doseq [feed (find-feeds)]
                  (delete-entity feed)))))
          (doseq [url urls]
            (let [syndfeed (get-syndfeed url)]
              (save-entity (feed {:user-id (:email(current-user))
                                  :original-url url
                                  :pillaged-feed "http://pillage.appspot.com/feeds/a3kdkfjjbjbj"
                                  :feed-name (. syndfeed getTitle)}))))
          (println "Populated the database with " (count (find-feeds)) " feeds"))))))

