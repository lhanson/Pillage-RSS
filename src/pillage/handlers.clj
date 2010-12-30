(ns pillage.handlers
  (:require [pillage.views :as views])
  (:use pillage.models
        [appengine.datastore :only (save-entity select string->key)]
        [appengine.users :only (current-user login-url logout-url)]
        [ring.util.response :only (redirect)])
  (:import
        (java.net URL)
        (com.sun.syndication.io SyndFeedInput XmlReader)))

(defn home [uri]
  "Default request handler"
  (if (nil? (current-user))
    (views/need-to-login (login-url uri))
    (views/home (:nickname (current-user)) (logout-url uri) (find-feeds))))

(defn get-syndfeed [url]
  "Returns a com.sun.syndication.io.SyndFeed for the given URL"
  (. (SyndFeedInput.) build (XmlReader. (URL. url))))

(defn add-feed [uri feed-url]
  "Adds a feed for the current user"
  (if (nil? (current-user))
    (views/need-to-login (login-url uri))
    (let [syndfeed (get-syndfeed feed-url)]
      (save-entity (feed {:user-id (:nickname (current-user))
                          :original-url feed-url
                          :pillaged-feed "http://pillage.appspot.com/feeds/a3kdkfjjbjbj"
                          :feed-name (. syndfeed getTitle)}))
      (redirect "/"))))

(defn get-feed [uri id]
  "Depending on parameters or content negotiation, returns either the edit page
   for the feed or the filtered RSS feed itself"
  (println "GET FEED with uri " uri " and ID " id)
  ; TODO: dispatch between different desired views (edit page vs. the RSS itself)
  ;       and only require login if we're editing
  (if (nil? (current-user))
    (views/need-to-login (login-url uri))
    (views/edit (:nickname (current-user)) (logout-url uri) (find-feeds))))

(defn delete-feed [id]
  "Deletes the specified feed"
  (if (current-user)
    (do
      (println "Deleting feed " (select "feed" (string->key id)))))
  ; TODO: handle the not-logged-in case more gracefully
  (redirect "/"))
