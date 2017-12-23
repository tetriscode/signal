(ns signal.components.http.processor
  (:require
   [signal.components.http.intercept :as intercept]
   [signal.components.http.response :as response]
   [clojure.data.json :as json]
   [xy.geojson :as geojson]
   [clojure.tools.logging :as log]
   [signal.components.processor :as processorapi]))

(defn http-get-all-processors
  "Returns http response of all processors"
  [processor-comp _]
  (log/debug "Getting all processors")
  (response/ok (processorapi/all processor-comp)))

(defn http-get-processor
  "Gets a processor by id"
  [processor-comp request]
  (log/debug "Getting processor by id")
  (let [id (get-in request [:path-params :id])]
    (if-let [processor (processorapi/find-by-id processor-comp id)]
      (response/ok processor)
      (let [err-msg (str "No processor found for id" id)]
        (log/warn err-msg)
        (response/ok err-msg)))))

(defn http-put-processor
  "Updates a processor using the json body"
  [processor-comp request]
  (log/debug "Updating processor")
  (let [t (:json-params request)]
    (log/debug "Validating processor")
    (let [processor (processorapi/modify processor-comp
                                         (get-in request [:path-params :id]) t)]
      (response/ok processor))))

(defn http-post-processor
  "Creates a new processor using the json body"
  [processor-comp request]
  (log/debug "Adding new processor")
  (let [t (:json-params request)]
    (log/debug "Validating processor")
    (let [processor (processorapi/create processor-comp t)]
      (response/ok processor))))

(defn http-delete-processor
  "Deletes a processor"
  [processor-comp request]
  (log/debug "Deleting processor")
  (let [id (get-in request [:path-params :id])]
    (processorapi/delete processor-comp id)
    (response/ok "success")))

(defn http-test-processor
  "HTTP endpoint used to test processors.  Takes a geojson feature
  in the json body as the feature to test"
  [processor-comp request]
  (if-let [params (:json-params request)]
    (do
      (processorapi/test-value processor-comp (geojson/parse params))
      (response/ok "success"))
    (response/bad-request "Request was empty")))

(defn routes [processor-comp]
  #{["/api/processors" :get
     (conj intercept/common-interceptors
           (partial http-get-all-processors processor-comp))
     :route-name :get-processors]
    ["/api/processors/:id" :get
     (conj intercept/common-interceptors
           (partial http-get-processor processor-comp))
     :route-name :get-processor]
    ["/api/processors/:id" :put
     (conj intercept/common-interceptors
           (partial http-put-processor processor-comp))
     :route-name :put-processor]
    ["/api/processors" :post
     (conj intercept/common-interceptors
           (partial http-post-processor processor-comp))
     :route-name :post-processor]
    ["/api/processors/:id" :delete
     (conj intercept/common-interceptors
           (partial http-delete-processor processor-comp))
     :route-name :delete-processor]
    ["/api/check" :post
     (conj intercept/common-interceptors
           (partial http-test-processor processor-comp))
     :route-name :http-test-processor]})