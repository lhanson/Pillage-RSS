(ns pillage.handlers
  (:use pillage.views)
  (:use [appengine.users :only (current-user login-url logout-url)]))

(defn home [uri]
  "Default request handler"
  (if (nil? (current-user))
    (need-to-login (login-url uri))
    (logged-in (:nickname (current-user)) (logout-url uri))))
