(ns pillage.models
  (:use appengine.datastore))

; For some unknown reason (some weird namespace collision?), using a name
; containing "Feed" gives me NoSuchMethodErrors when creating one. In the
; interests of getting something done I've renamed it for now.
(defentity Pillagefeed ()
  ((user-id)
   (original-url)
   (feed-name)
   (exclusion-filters)
   (modification-filters)))

; A grouping of filters within a category
(defentity FilterGroup ()
  ((user-id)
   (label)
   (exclusive) ; whether the filters are mutually exclusive
   (filters)))

; A filter which can exclude feed items
(defentity ExclusionFilter ()
  ((user-id)
   (label)
   (type)
   (regex)))

; A filter which modifies the content of feed items
(defentity ModificationFilter ()
  ((user-id)
   (label)
   (type)
   (regex)))

