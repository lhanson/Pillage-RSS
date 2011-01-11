(ns pillage.feed-handling
  (:import
    (java.net URL)
    (com.sun.syndication.io SyndFeedInput XmlReader)))

(defn get-syndfeed [url]
  "Returns a com.sun.syndication.io.SyndFeed for the given URL"
  (. (SyndFeedInput.) build (XmlReader. (URL. url))))

