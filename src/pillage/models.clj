(ns pillage.models
  (:use appengine.datastore))
  ;(:use [appengine.datastore :only (defentity)]))

; TODO: how do I persist a child entity (FeedTransformation) and then be able to
; load it based on the id of the parent?
; IRC says don't do either, just generate the child's key (make it "1", then I'll
; always know what the child's key is appid_pillagefeed_1_feed-transformation-1

; For some unknown reason (some weird namespace collision?), using a name
; containing "Feed" gives me NoSuchMethodErrors when creating one. In the
; interests of getting something done I've renamed it for now.
(defentity Pillagefeed ()
  ((user-id)
   (original-url)
   (pillaged-feed)
   (feed-name)))

(defentity FeedTransformation (Pillagefeed)
  ((name)
   (strip-items)))

