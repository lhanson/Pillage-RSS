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

(defn- assert-status [status response]
  "Verifies that the expected status code matches that of the given response"
  (is (= status (:status response))))

(defn- assert-logged-in []
  "Verifies test setup for tests which depend on a user being logged in"
  (is (not (nil? (current-user))) "Test relies on an authenticated user"))

(defn- assert-not-logged-in []
  "Verifies test setup for tests which depend on an unauthenticated user"
  (is (nil? (current-user)) "Test relies on no user signed in"))

(defn- assert-redirects-to-root [response]
  "Verifies that the given Ring response is a redirect to the login page"
  (and
    (assert-status 302 response)
    (is (= "/" (get (:headers response) "Location")))))

(defmacro with-local-user-and-data [& body]
  "Sets up a mock user for the local test environment"
  `(try (.setUp (doto (LocalServiceTestHelper. (into-array [(LocalDatastoreServiceTestConfig.)]))
                  (.setEnvAuthDomain "mockAuthDomain")
                  (.setEnvIsLoggedIn true)
                  (.setEnvIsAdmin true)
                  (.setEnvEmail "mock_email@foo.com")))
     ~@body))

(def mockFeedEntity
  (feed {:user-id (:nickname "mock_user")
         :original-url "http://mock/feed/url"
         :pillaged-feed "http://pillage.appspot.com/feeds/mock_feed_url"
         :feed-name "Mock RSS Feed"}))

(def mockSyndFeed (proxy [SyndFeed] []
                     (getTitle [] "Mock Feed Title")))

(user-test test-routes
  (assert-status 200 (request "/"))
  (assert-status 404 (request "/bogus-url")))

(datastore-test test-save-feed-entity
  (is (= 0 (count (find-feeds))))
  (save-entity mockFeedEntity)
  (is (= 1 (count (find-feeds)))))

(deftest add-feed
  (with-local-user-and-data
    (assert-logged-in)
    (is (= 0 (count (find-feeds))))
    (expect [get-syndfeed (returns mockSyndFeed)] ; Mock out the SyndFeed
      (assert-status 404 (request "/feeds/nonexistent" :method :get))
      (assert-redirects-to-root (request "/feeds" :method :post :params {"feed_url" "http://bogus.url"}))
      (let [id (.getId (:key (first (find-feeds))))]
        (println "Trying to get the feed ID " id)
        (assert-status 200 (request (str "/feeds/" id)))
        ; TODO: assert that the response body contains the newly-added key ID
        ))))

(user-test add-feed-anauthenticated
  "Any unauthenticated request should redirect to the login page"
  (assert-not-logged-in)
  (let [response (request "/feeds" :method :post :params {"feed_url" "http://bogus.url"})]
    (and (assert-status 200 response)
         (is (re-find #"Log in" (:body response))))))

(user-test delete-feed
  (with-local-user-and-data
    (assert-logged-in)
    (save-entity mockFeedEntity)
    (let [id (.getId (:key (first (find-feeds))))]
      (is false "Not yet implemented")
      ;(println "Stored key " id)
      ;TODO: call DELETE twice on this ID and assert that we get the expected results
      )))

(user-test delete-feed-unauthenticated
  "Double check that all unauthenticated requests are redirected to the login page"
  (assert-not-logged-in)
  (assert-redirects-to-root (request "/feeds/1" :method :delete)))
