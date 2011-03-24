(ns pillage.models
  (:use appengine.datastore))

; For some unknown reason (some weird namespace collision?), using a name
; containing "Feed" gives me NoSuchMethodErrors when creating one. In the
; interests of getting something done I've renamed it for now.
(defentity Pillagefeed ()
  ((user-id)
   (original-url)
   (feed-name)
   (exclusion-filters)       ; keys of the exclusion filters assigned
   (modification-filters)))  ; keys of the modification filters assigned

; A grouping of filters within a category
(defentity FilterGroup ()
  ((author)
   (label)
   (exclusive) ; whether the filters are mutually exclusive
   (filters)))

; A filter which can exclude feed items
(defentity ExclusionFilter ()
  ((author)
   (label)
   (type)
   (regex)))

; A filter which modifies the content of feed items
(defentity ModificationFilter ()
  ((author)
   (label)
   (type)
   (regex)))

