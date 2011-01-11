(ns pillage.core-test
  (:use pillage.core :reload
        pillage.models
        [pillage.feed-handling :only (get-syndfeed)]
        clojure.test
        clojure.contrib.mock.test-adapter
        appengine.test
        appengine.users
        appengine.datastore.keys
        appengine.datastore.protocols
        appengine.datastore.query)
  (import
        com.google.appengine.api.users.dev.LocalUserService
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
  "Verifies that the given Ring response is a redirect to the root page"
  (and
    (assert-status 302 response)
    (is (= "/" (get (:headers response) "Location")))))

(def localServiceTestHelper
  (LocalServiceTestHelper. (into-array [(LocalDatastoreServiceTestConfig.)])))

(def mockUserName "mock_user@foo.com")

(defmacro with-local-user-and-data [& body]
  "Sets up a mock user for the local test environment"
  `(try (.setUp (doto localServiceTestHelper
                  (.setEnvAuthDomain "mockAuthDomain")
                  (.setEnvIsLoggedIn true)
                  (.setEnvIsAdmin true)
                  (.setEnvEmail mockUserName)))
     (assert-logged-in)
     ~@body))

(defmacro switch-users [username & body]
  "Switches the logged-in user for the local test environment"
  `(try (. localServiceTestHelper (setEnvEmail ~username))
     ~@body))

(def mockFeedEntity
  (feed {:user-id mockUserName
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

(datastore-test test-query
  (save-entity mockFeedEntity)
  (save-entity (feed {:user-id "bogus user"
                      :original-url "original url"
                      :pillaged-feed "pillaged feed"
                      :feed-name "feed name"}))
  (is (= 2 (count (execute (query "feed"))))
      "Should have stored two feeds total")
  (is (= 1
         (count (execute (filter-by (query "feed") = :user-id (:user-id mockFeedEntity)))))
      "Should have found one test for mock user"))

(deftest get-nonexistent-feed
  "Exercises the get-feed handler, making sure nonexistent feeds return 404"
  (with-local-user-and-data
    (expect [get-syndfeed (returns mockSyndFeed)] ; Mock out the SyndFeed
      (assert-status 404 (request "/feeds/nonexistent" :method :get)))))

(deftest add-feed
  (with-local-user-and-data
    (is (= 0 (count (find-feeds))))
    (expect [get-syndfeed (returns mockSyndFeed)] ; Mock out the SyndFeed
      (assert-redirects-to-root (request "/feeds" :method :post :params {"feed_url" "http://bogus.url"}))
      (let [id (key->string (:key (first (find-feeds))))]
        (assert-status 200 (request (str "/feeds/" id)))
        (switch-users "second_user@email.com"
          (assert-status 404 (request (str "/feeds/" id))))))))

(user-test add-feed-anauthenticated
  "Any unauthenticated request should be shown the login page"
  (assert-not-logged-in)
  (let [response (request "/feeds" :method :post :params {"feed_url" "http://bogus.url"})]
    (and (assert-status 200 response)
         (is (re-find #"Log in" (:body response))))))

(user-test delete-feed
  (with-local-user-and-data
    (save-entity mockFeedEntity)
    (let [id (key->string (:key (first (find-feeds))))]
      (assert-redirects-to-root (request (str "/feeds/" id) :method :delete))
      (is (= 0 (count (find-feeds))))
      (assert-status 404 (request (str "/feeds/" id) :method :delete)))))

(user-test delete-feed-unauthenticated
  "Double check that all unauthenticated requests are shown the login page"
  (with-local-datastore
    (assert-not-logged-in)
    (save-entity mockFeedEntity)
    (let [id (key->string (:key (first (find-feeds))))]
      (assert-status 200 (request (str "/feeds/" id) :method :delete))
      (is (= 1 (count (find-feeds))) "Unauthenticated user should not be able to delete feeds"))))

