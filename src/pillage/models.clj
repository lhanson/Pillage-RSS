(ns pillage.models
  (:use appengine.datastore))

(defentity Feed ()
  ((user-id)
   (original-url)
   (pillaged-feed)
   (feed-name)))
