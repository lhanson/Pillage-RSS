(ns pillage.feed-handling
  (:import
    (java.net URL)
    (com.sun.syndication.io SyndFeedInput SyndFeedOutput XmlReader)))

(defn get-syndfeed [url]
  "Returns a com.sun.syndication.io.SyndFeed for the given URL"
  (. (SyndFeedInput.) build (XmlReader. (URL. url))))

(defn get-rss [syndfeed]
  "Returns a string representing the RSS content of the given feed"
  (.. (SyndFeedOutput.) (outputString syndfeed)))
