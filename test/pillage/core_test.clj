(ns pillage.core-test
  (:use pillage.core :reload
        pillage.models
     [pillage.handlers :only (get-syndfeed)]
        clojure.test
        clojure.contrib.mock.test-adapter
        appengine.test
        appengine.users
        appengine.datastore)
  (import
        com.google.appengine.tools.development.testing.LocalServiceTestHelper
        com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
        com.sun.syndication.feed.synd.SyndFeed))

(defn- request
  "Performs a Ring request on the specified web app"
  ([resource &{ :keys [method params]
                :or {method :get params {}}}]
    (pillage-rss {:request-method method :uri resource :params params})))

(user-test test-routes
  (is (= 200 (:status (request "/"))))
  (is (= 404 (:status (request "/bogus-url")))))


(datastore-test test-save-feed-entity
  (is (= 0 (count (find-feeds))))
  (save-entity (feed {:user-id (:nickname "mock_user")
                      :original-url "http://mock/feed/url"
                      :pillaged-feed "http://pillage.appspot.com/feeds/mock_feed_url"
                      :feed-name "Mock RSS Feed"}))
  (is (= 1 (count (find-feeds)))))

(defmacro with-local-user-and-data [& body]
  "Sets up a mock user for the local test environment"
  `(try (.setUp (doto (LocalServiceTestHelper. (into-array [(LocalDatastoreServiceTestConfig.)]))
                  (.setEnvAuthDomain "mockAuthDomain")
                  (.setEnvIsLoggedIn true)
                  (.setEnvIsAdmin true)
                  (.setEnvEmail "mock_email@foo.com")))
     ~@body))

(def mockSyndFeed (proxy [SyndFeed] []
                     (getTitle [] "Mock Feed Title")))

(deftest add-feed
  (with-local-user-and-data
    (is (not (= nil (current-user))) "Test relies on an authenticated user")
    (expect [get-syndfeed (returns mockSyndFeed)] ; Mock out the SyndFeed
      (let [response (request "/feeds" :method :post :params {"feed_url" "http://bogus.url"})]
        (and
          (is (= 302 (:status response)))
          (is (= "/" (get (:headers response) "Location"))))))))

(user-test add-feed-anauthenticated
  (is (nil? (current-user)) "Test relies on no user signed in")
  (let [response (request "/feeds" :method :post :params {"feed_url" "http://bogus.url"})]
    (and (is (= 200 (:status response)))
         (is (re-find #"Log in" (:body response))))))
