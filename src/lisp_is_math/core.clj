(ns lisp-is-math.core
  (:require [clojure.math :as math]))

(defmacro define-element-wise [function fn-name tensor tensor2]
  `(defn ~(symbol (str fn-name)) [~tensor ~tensor2]
     (cond (number? ~tensor)
           (map (fn [x#] (~function x# ~tensor)) ~tensor2)
           (number? ~tensor2)
           (map (fn [x#] (~function x# ~tensor2)) ~tensor)
           :else
           (map ~function ~tensor ~tensor2))))

(defmacro define-element-wise-one-param [function function-name t]
  `(defn ~(symbol (str function-name)) [~t]
     (map (fn [x#] (~function x#)) ~t)))

;;; vector arithmetic

(define-element-wise + add-vectors v v2)
(define-element-wise - sub-vectors v v2)

;; dot product

(define-element-wise * mul-vectors v v2)
(defn dot-product [v v2]
  (reduce + (mul-vectors v v2)))

;; matrix by scalar

(define-element-wise mul-vectors mul-matrix m1 m2)

;; matrix arithmetic

(define-element-wise add-vectors add-matrices m m2)
(define-element-wise sub-vectors  sub-matrices m m2)

;; power

(define-element-wise math/pow pow-vectors v v2)
(define-element-wise pow-vectors pow-matrices m m2)

;; max

(define-element-wise max max-vectors v v2)
(define-element-wise max-vectors max-matrices m m2)

;; min

(define-element-wise min min-vectors v v2)
(define-element-wise min-vectors min-matrix m1 m2)

;; exp

(define-element-wise-one-param math/exp exp-vectors t)
(define-element-wise-one-param exp-vectors exp-matrice t)

;; log

(define-element-wise-one-param math/log log-vector t)
(define-element-wise-one-param log-vector log-matrix t)

;; abs

(define-element-wise-one-param abs abs-vector t)
(define-element-wise-one-param abs-vector abs-matrix t)

;; log10

(define-element-wise-one-param  math/log10 log10-vector t)
(define-element-wise-one-param log10-vector log10-matrix t)

