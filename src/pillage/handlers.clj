(ns pillage.handlers
  (:require [pillage.views :as views])
  (:use pillage.models
        [pillage.feed-handling :only (get-syndfeed get-rss)]
        [appengine.datastore :only (save-entity update-entity delete-entity
                                    select string->key deserialize-entity)]
        [appengine.datastore.keys :only (make-key)]
        [appengine.datastore.protocols :only (execute)]
        [appengine.datastore.query :only (filter-by query)]
        [appengine.datastore.service :only (get-entity)]
        [appengine.users :only (current-user login-url logout-url)]
        [ring.util.response :only (redirect)]))

(defn- load-feeds [userid]
  "Returns the stored feeds for the given user"
  (execute (filter-by (query "pillagefeed") = :user-id userid)))

(defn- load-feed [userid feed-id]
  "Returns the user's specified feed"
  (try
    (if-let [feed (get-entity (string->key feed-id))]
      (if (= userid (get (.getProperties feed) "user-id"))
        (deserialize-entity feed)))
    (catch Exception e
      (println "Error loading feed" feed-id "for user" userid ": " e))))

(defn- delete-feed- [userid feed-id]
  "Removes the user's feed specified by id.

   Returns whether the feed was found and deleted."
  (if-let [feed (load-feed userid feed-id)]
    (delete-entity feed)))

(defn- get-transformation-key [feed]
  ; Assuming every feed will only have one transformation, we can
  ; generate the key for it without actually querying.
  (make-key (:key feed) "feed-transformation" 1))

(defn- load-transformation [feed]
  "Returns the transformation associated with the specified feed"
  ; Since we already enforce user-level access to the parent feed,
  ; we don't need to ensure that this is the current user's transformation.
  (if-let [entity (get-entity (get-transformation-key feed))]
    (deserialize-entity entity)
    (do
      (println "!!!!!!!!!!!!!!!!!!!Creating default transformation feed from" feed)
      (let [transformation (feed-transformation feed {:name (:feed-name feed)})
          transformation-key (get-transformation-key feed)]
      (println "!!!!!!!Creating transformation entity")
      (save-entity (assoc transformation :key transformation-key))
      (assoc transformation :key transformation-key)))))

(defn- update-transformation [userid feed-id params]
  "Updates the specified feed with the provided transformation"
  (let [feed (load-feed userid feed-id)
        transformation-params (into {} (for [[k v] params] [k (str v)])) ]
    (if-let [child-entity (load-transformation feed)]
      (update-entity child-entity transformation-params))))
     ; (let [transformation (feed-transformation feed transformation-params)
     ;       transformation-key (get-transformation-key feed)]
     ;   (println "Creating transformation entity")
     ;   (save-entity (assoc transformation :key transformation-key))))))

(defn home [uri]
  "Default request handler"
  (if (nil? (current-user))
    (views/need-to-login (login-url uri))
    (let [nickname (:nickname (current-user))]
      (views/home nickname (logout-url uri) (load-feeds nickname)))))

(defn add-feed [uri feed-url]
  "Adds a feed for the current user"
  (if (nil? (current-user))
    (views/need-to-login (login-url uri))
    (let [syndfeed (get-syndfeed feed-url)]
      (save-entity (pillagefeed {:user-id (:nickname (current-user))
                                 :original-url feed-url
                                 ; TODO: probably don't need this field, remove from model
                                 :pillaged-feed "http://pillage.appspot.com/feeds/a3kdkfjjbjbj"
                                 :feed-name (. syndfeed getTitle)}))
      (redirect "/"))))

(defn edit-feed [uri id]
  "Depending on parameters or content negotiation, returns either the edit page
   for the feed or the filtered RSS feed itself"
  (if (nil? (current-user))
    (views/need-to-login (login-url uri))
    (let [nickname (:nickname (current-user))]
      (if-let [feed (load-feed nickname id)]
        (if-let [transformation (load-transformation feed)]
          (views/edit nickname (logout-url uri) feed transformation)
          ; Otherwise create a default transformation
          (let [transformation (feed-transformation feed {:name (:feed-name feed)})]
            (views/edit nickname (logout-url uri) feed transformation)))))))

(defn update-feed [uri id transformation-params]
  "Updates the feed specified by *id* to use the provided transformation"
  (if (nil? (current-user))
    (views/need-to-login (login-url uri))
    (update-transformation (:nickname (current-user)) id transformation-params)))

(defn delete-feed [uri id]
  "Deletes the specified feed"
  (if (nil? (current-user))
    (views/need-to-login (login-url uri))
    (if (delete-feed- (:nickname (current-user)) id)
      (redirect "/"))))

(defn get-feed [uri id]
  "Returns the pillaged version of the RSS feed"
  ; TODO: this should not require the user to be logged in
  (let [nickname (:nickname (current-user))]
    (if-let [feed (load-feed nickname id)]
      (if-let [transformation (load-transformation feed)]
        (let [syndfeed (get-syndfeed (:original-url feed))]
          (println "Loading RSS for" feed)
          (println "Syndfeed:" syndfeed)
          {:headers {"Content-Type" "text/xml"}
           :body (get-rss syndfeed)})
        (println "Error loading feed transformation for" id))
      (println "Error loading feed" id))))
      ; TODO: redirect to a 404

