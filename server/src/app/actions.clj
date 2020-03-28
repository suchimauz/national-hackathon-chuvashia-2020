(ns app.actions
  (:require [clj-pg.honey  :as pg]
            [json-schema.core :as schema]
            [honeysql.core :as hsql]))

(defn ok          [response] {:status 200 :body response})
(defn created     [response] {:status 201 :body response})
(defn bad-request [response] {:status 522 :body {:errors {:message "Unprocessable Entity"
                                                          :detail response}}})
(defn res-not-found [id] {:status 404 :body {:errors {:message (str "Resource id = " id " not found")}}})

(defn validation [body {validator :validator}]
  (schema/validate validator body))

(defn row-to-resource [res]
  (-> res
      (assoc-in [:resource :id] (:id res))
      (assoc-in [:resource :resourceType] (:resource_type res))
      :resource))

(defn -get [table {:keys [params] db :db/connection {:keys [id]} :path-params}]
  (let [query (merge
               {:select [:*]
                :from   [(:table table)]
                :limit  (or (:count params) 100)}
               (when id {:where [:= :id id]}))]
    (if id
      (let [res (:resource (pg/query-first db query))]
        (if-not res
          (res-not-found id)
          (ok res)))
      (ok (->> query
               (pg/query db)
               (map row-to-resource))))))

(defn -post [table {:keys [body] db :db/connection}]
  (let [result (validation body table)]
    (if (empty? (:errors result))
      (created (row-to-resource (pg/create db table {:resource (dissoc body :id :resourceType)})))
      (bad-request (:errors result)))))

(defn -put [table {:keys [body] db :db/connection {:keys [id]} :path-params}]
  (let [result (validation body table)]
    (if (empty? (:errors result))
      (ok (row-to-resource (pg/update db table {:id id :resource (dissoc body :id :resourceType)})))
      (bad-request (:errors result)))))

(defn -delete [table {:keys [params] db :db/connection {:keys [id]} :path-params}]
  (let [response (pg/delete db table id)]
    (ok {:id (:id response)})))
