(ns example.handler
  (:require
   [compojure.route :as route]
   [compojure.core :refer [defroutes GET POST ANY]]
   [ring.util.response :refer [response content-type charset]]
   [ring.middleware.format :refer [wrap-restful-format]]
   [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
   [example.db :refer [save-director<! delete-director! directors]]
   [example.api :refer [get-director-of-movie]]
   [reloaded.repl :refer [system]]))


(defroutes routes
  (GET "/" [] "Welcome")
  (POST "/movie" req (fn [{params :params :as req}]
                       (-> (pr-str {:director (get-director-of-movie (:movie params))})
                           response
                           (content-type "application/edn")
                           (charset "UTF-8"))))
  (GET "/directors" [] (fn [_]
                         (let [db (:db-spec (:db reloaded.repl/system))]
                           (-> (pr-str (map :name (directors db)))
                               response
                               (content-type "application/edn")
                               (charset "UTF-8")))))
  (ANY "/director" req (fn [{params :params :as req}]
                         (let [db (:db-spec (:db system))]                       
                           (->
                            (case (:request-method req)
                              :put (save-director<! db (:director params))
                              :delete (delete-director! db (:director params)))
                            response))))
  (route/not-found "404"))

(def app
  (-> routes
      wrap-restful-format
      (wrap-defaults api-defaults)))
