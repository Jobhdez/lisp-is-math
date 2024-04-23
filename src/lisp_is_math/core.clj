(ns lisp-is-math.core
  (:require [clojure.math :as math]))

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
    (map (fn [row]
           (map (fn [col]
                  (dot-product row col)) (transpose m2))) m)))

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

