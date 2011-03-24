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

(defn- load-filter [filter-id]
  "Loads the filter corresponding to the given id"
  (try
    (if-let [filter (get-entity (string->key filter-id))]
      (deserialize-entity filter))
    (catch Exception e
      (println "Error loading filter" filter-id))))

;(defn- load-filters-for-author[userid]
;  "Returns the filters created by the given user. Typical use would be to load
;   the default filters available to every user."
;  (defn load-filters-for-group [group]
;    (assoc group :filters (map load-filter (:filters group))))
;  ; TODO: if this is a problem name-wise, try ExclusionFilter and ModificationFilter
;  (let [groups (execute (filter-by (query "filter-group") = :author userid))]
;    (map load-filters-for-group groups)))

(defn- load-filters [feed]
  "Loads the filters associated with the specified feed, associating them with
   the returned instance of the feed"
  (let [exclusions    (map load-filter (:exclusion-filters feed))
        modifications (map load-filter (:modification-filters feed))]
    (println "Exclusions" exclusions ", modifications" modifications)
    (if (and (empty? exclusions) (empty? modifications))
      (println "Feed is not assigned to any filters")
      )
    (assoc feed :exclusions exclusions :modifications modifications)
    ))

(defn- load-feed [userid feed-id]
  "Returns the user's specified feed"
  (try
    (if-let [feed (get-entity (string->key feed-id))]
      (if (= userid (get (.getProperties feed) "user-id"))
        (load-filters (deserialize-entity feed))))
    (catch Exception e
      (println "Error loading feed" feed-id "for user" userid ": " e))))

(defn- delete-feed- [userid feed-id]
  "Removes the user's feed specified by id.

   Returns whether the feed was found and deleted."
  (if-let [feed (load-feed userid feed-id)]
    (delete-entity feed)))

;(defn- get-transformation-key [feed]
;  ; Assuming every feed will only have one transformation, we can
;  ; generate the key for it without actually querying.
;  (make-key (:key feed) "feed-transformation" 1))

;(defn- update-filters [userid feed-id params]
;  "Updates the specified feed with the provided transformation"
;  (let [feed (load-feed userid feed-id)
;        transformation-params (into {} (for [[k v] params] [k (str v)])) ]
;    (if-let [child-entity (load-filters feed)]
;      (update-entity child-entity transformation-params))))
;     ; (let [transformation (feed-transformation feed transformation-params)
;     ;       transformation-key (get-transformation-key feed)]
;     ;   (println "Creating transformation entity")
;     ;   (save-entity (assoc transformation :key transformation-key))))))

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
                                 :feed-name (. syndfeed getTitle)}))
      (redirect "/"))))

(defn edit-feed [uri id]
  "Depending on parameters or content negotiation, returns either the edit page
   for the feed or the filtered RSS feed itself"
  (if (nil? (current-user))
    (views/need-to-login (login-url uri))
    (let [nickname (:nickname (current-user))]
      (if-let [feed (load-feed nickname id)]
        (if-let [filters (load-filters feed)]
          (views/edit nickname (logout-url uri) feed filters))))))

(defn update-feed [uri id transformation-params]
  "Updates the feed specified by *id* to use the provided transformation"
  (if (nil? (current-user))
    (views/need-to-login (login-url uri))
    ;(update-filters (:nickname (current-user)) id transformation-params)))
    ))

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
      (if-let [filters (load-filters feed)]
        (let [syndfeed (get-syndfeed (:original-url feed))]
          (println "Loading RSS for" feed)
          (println "Syndfeed:" syndfeed)
          (println "RSS:" (get-rss syndfeed))
          {:headers {"Content-Type" "text/xml"}
           :body (get-rss syndfeed)})
        (println "Error loading filters for feed" id))
      (println "Error loading feed" id))))
      ; TODO: redirect to a 404

