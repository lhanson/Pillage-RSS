(ns pillage.handlers
  (:require [pillage.views :as views])
  (:use pillage.models
        [appengine.datastore :only (save-entity delete-entity select string->key)]
        [appengine.datastore.keys :only (make-key)]
        [appengine.datastore.protocols :only (execute)]
        [appengine.datastore.query :only (filter-by query)]
        [appengine.datastore.service :only (get-entity)]
        [appengine.users :only (current-user login-url logout-url)]
        [ring.util.response :only (redirect)])
  (:import
        (java.net URL)
        (com.sun.syndication.io SyndFeedInput XmlReader)))

(defn- load-feeds [userid]
  "Returns the stored feeds for the given user"
  (execute (filter-by (query "feed") = :user-id userid)))

(defn- load-feed [userid feed-id]
  "Returns the user's specified feed"
  (try
    (if-let [feed (get-entity (make-key "feed" (Integer/parseInt feed-id)))]
      (if (= userid (get (.getProperties feed) "user-id"))
        feed))
    (catch Exception e
      (println "Error loading feed " feed-id " for user " userid ": " e))))

(defn- delete-feed- [userid id]
  "Removes the user's feed specified by id.

   Returns whether the feed was found and deleted."
  (if-let [feed (load-feed userid id)]
    (delete-entity feed))
  )

(defn home [uri]
  "Default request handler"
  (if (nil? (current-user))
    (views/need-to-login (login-url uri))
    (let [nickname (:nickname (current-user))]
      (views/home nickname (logout-url uri) (load-feeds nickname)))))

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
  ; TODO: dispatch between different desired views (edit page vs. the RSS itself)
  ;       and only require login if we're editing
  (if (nil? (current-user))
    (views/need-to-login (login-url uri))
    (let [nickname (:nickname (current-user))]
      (if-let [feed (load-feed nickname id)]
        (views/edit nickname (logout-url uri) nickname)))))

(defn delete-feed [uri id]
  "Deletes the specified feed"
  (if (nil? (current-user))
    (views/need-to-login (login-url uri))
    (if (delete-feed- (:nickname (current-user)) id)
      (redirect "/"))))
