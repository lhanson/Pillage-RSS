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

; A grouping of mutually exclusive filters
(defentity FilterGroup (Pillagefeed)
  ((label)
   (filters)))

; A filter which can exclude feed items
(defentity ExclusionFilter (Pillagefeed)
  ((label)
   (type)
   (regex)))

; A filter which modifies the content of feed items
(defentity ModificationFilter (Pillagefeed)
  ((label)
   (type)
   (regex)))

