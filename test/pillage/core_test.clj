(ns pillage.core-test
  (:use [pillage.core :reload]
        clojure.test
        [appengine.test :only (user-test)]))

(defn- request [resource web-app & params]
  "Performs a Ring request on the specified web app"
  (web-app {:request-method :get :uri resource :params (first params)}))

(user-test test-routes
  (is (= 200 (:status (request "/" pillage-rss)))))

