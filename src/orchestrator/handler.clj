(ns orchestrator.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [org.httpkit.client :as http]
            [ring.middleware.json :as middleware]
            [compojure.handler :as handler])
  (:use ring.util.response))

(defn add-to-response [response-map response-future]
  (println "add-to-response")
  (println response-map)
  (conj response-map {:resp-status (:status @response-future)} ))

(defn get-multiple-urls [urls]
  (println "entering function")
  (println (get urls "url"))
  (let [futures (doall (map http/get (get urls "url")))]

    (doseq [resp futures]
      (println (-> @resp :opt :url) " status: " (:status @resp) ))
    (response (reduce add-to-response {} futures)))

  )

(defroutes app-routes
           (GET "/orchestrate" {params :query-params} (get-multiple-urls params))
           (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)))
