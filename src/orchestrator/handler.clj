(ns orchestrator.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [org.httpkit.client :as http]
            [ring.middleware.json :as middleware]
            [compojure.handler :as handler])
  (:use ring.util.response))

(defn generate-response-body [response-future]
  {:status (:status @response-future)
   :body (:body @response-future)}
  )

(defn add-to-response [response-map response-future]
  (let [response-key (get response-future 0)
        response-promise (get response-future 1)]
    (conj response-map {response-key (generate-response-body response-promise)})))

(defn submit-single-request [result parameter]
  (conj result {(get parameter 0) (http/get (get parameter 1))}))

(defn submit-all-requests [params]
  (reduce submit-single-request {} params))

(defn build-response [futures]
  (reduce add-to-response {} futures))

(defn get-multiple-urls [params]
  (let
    [futures (submit-all-requests params)]
    (response (build-response futures))))

(defroutes app-routes
           (GET "/orchestrate" {params :query-params} (get-multiple-urls params))
           (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)))