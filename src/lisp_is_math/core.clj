(ns lisp-is-math.core
  (:require [clojure.math :as math]
            [cheshire.core :refer [generate-string parse-string]]
            [org.httpkit.server :as server]
            [compojure.route :as route]
            [clojure.string :as str]
            [clojure.data.json :as json]
            [compojure.core :refer [defroutes GET POST PUT DELETE]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [next.jdbc :as jdbc]))

(defmacro define-element-wise [function fn-name]
  `(defn ~(symbol (str fn-name)) [tensor# tensor2#]
     (cond (number? tensor#)
           (map (fn [x#] (~function x# tensor#)) tensor2#)
           (number? tensor2#)
           (map (fn [x#] (~function x# tensor2#)) tensor#)
           :else
           (map ~function tensor# tensor2#))))

(defmacro define-element-wise-one-param [function function-name]
  `(defn ~(symbol (str function-name)) [t#]
     (map (fn [x#] (~function x#)) t#)))

;;; vector arithmetic

(define-element-wise + add-v)
(define-element-wise - sub-v)

;; dot product

(define-element-wise * mul-v)

(defn dot-product [v v2]
  (reduce + (mul-v v v2)))

;;; matrix matrix multiplication

(defn mat-mul [m m2]
  (letfn [(transpose [m] (apply mapv vector m))]
    (map (fn [row] (map (fn [col] (dot-product row col))
                        (transpose m2)))
         m)))

;; matrix  by scalar

(define-element-wise mul-v mul-m)

;; matrix arithmetic

(define-element-wise add-v add-m)
(define-element-wise sub-v  sub-m)

;; power

(define-element-wise math/pow pow-v)
(define-element-wise pow-v pow-m)

;; max

(define-element-wise max max-v)
(define-element-wise max-v max-m)

;; min

(define-element-wise min min-v)
(define-element-wise min-v min-m)

;; exp

(define-element-wise-one-param math/exp exp-v)
(define-element-wise-one-param exp-v exp-m)

;; log

(define-element-wise-one-param math/log log-v)
(define-element-wise-one-param log-v log-m)

;; abs

(define-element-wise-one-param abs abs-v)
(define-element-wise-one-param abs-v abs-m)

;; log10

(define-element-wise-one-param  math/log10 log10-v)
(define-element-wise-one-param log10-v log10-m)

;;;; server

(def db-config
  {:dbtype "postgresql"
   :dbname "lalgdb"
   :host "localhost"
   :user "lara"
   :password "hello123"})

(def db (jdbc/get-datasource db-config))

(defn add [op]
  (fn [req]
    (let [body (slurp (:body req))
          body-params (parse-string body true)]
      (println "body-params" body-params)
      {:status 200
       :headers {"Content-Type" "text/json"}
       :body (-> (let [e1 (read-string (body-params :e1))
                       e2 (read-string (body-params :e2))]
                   (println "e1" e1)
                   (println "e2" e2)
                   (println "type" (type e1))
                   (let [result (op e1 e2)]
                     (println "result" result)
                     (println "result-type" (type result))
                     (let [db-exp (format "insert into lalg_exps(e1, e2, result) values('%s', '%s', '%s')"
                                          e1
                                          e2
                                          (vec result))]
                       (println "db-exp" db-exp)
                       (jdbc/execute! db [db-exp])
                       (json/write-str {:e1 e1 :e2 e2 :result (vec result)})))))})))

(def add-vectors-handler (add add-v))
(def sub-vectors-handler (add sub-v))
(def add-matrices-handler (add add-m))
(def sub-matrices-handler (add sub-m))

(defn get-lalg-exps [req]
  (let [exps (jdbc/execute! db ["select * from lalg_exps"])]
    (println "exps" exps)
    {:status 200
     :headers {"Content-Type" "text/json"}
     :body (json/write-str exps)}))

(defn get-exp [req]
  (let [id (-> req :params :id)]
    {:status 200
     :headers {"Content-Type" "text/json"}
     :body (json/write-str (jdbc/execute! db [(format "select * from lalg_exps where id=%s" id)]))}))

(defn delete-exp [req]
  (let [id (-> req :params :id)]
    (jdbc/execute! db [(format "delete from lalg_exps where id=%s" id)])
    {:status 200
     :headers {"Content-Type" "text/json"}
     :body (json/write-str {:ok "delete success"})}))

(defroutes app-routes
  (POST "/api/vectors/add" [] add-vectors-handler)
  (POST "/api/vectors/sub" [] sub-vectors-handler)
  (POST "/api/matrices/add" [] add-matrices-handler)
  (POST "/api/matrices/sub" [] sub-matrices-handler)
  (GET "/api/exps" [] get-lalg-exps)
  (GET "/api/exp" [] get-exp)
  (DELETE "/api/exp/delete" [] delete-exp))

(defn -main
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "4000"))]
    (server/run-server
     (-> app-routes
         (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false)))
     {:port port})
    (println (str "Webserver started at http://127.0.0.1:" port "/"))))
