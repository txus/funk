(ns funk.core-test
  (:require [clojure.test :refer :all]
            [funk.core :refer :all]
            [funk.verify :refer :all]))

(defrecord Maybe [value])
(defrecord List [wrapped])

(deftest maybe-monad-test
  (testing "The Maybe monad can be constructed"
    (extend-type Maybe
      Monad
      (>>= [mv f]
        (if (:value mv)
          (f (:value mv))
          (Maybe. nil))))

    (defmethod return Maybe [_ v]
      (Maybe. v))

    (let [mv (Maybe. 10)
          f #(Maybe. (inc %))
          g #(Maybe. (* 2 %))
          x 10]
      (is (monad? mv f g x)))))

(deftest list-functor-test
  (testing "The List functor can be constructed"
    (extend-type List
      Functor
      (fmap [functor f]
        (List. (map f (:wrapped functor)))))
    (let [functor (List. [1 2 3])
          f inc
          g (partial * 2)]
      (is (functor? functor f g)))))

(deftest list-applicative-functor-test
  (testing "The List applicative"
    (extend-type List
      Functor
      (fmap [functor f]
        (List. (map f (:wrapped functor))))) ; we need it to be a functor
    (defmethod pure List [_ v]
      (List. [v]))
    (defmethod <*> List [fs xs]
      (List. (for [f (:wrapped fs)
                   x (:wrapped xs)]
               (f x))))
    (let [functor (List. [1 2 3])
          f inc
          g (partial * 2)
          x 10]
      (is (applicative-functor? functor f g x)))))

(deftest list-monoid-test
  (testing "List is a lawful monoid"
    (extend-type List
      Monoid
      (mempty [_] (List. []))
      (mappend [x y] (List. (concat (:wrapped x) (:wrapped y)))))
    (let [monoid (List. [1 2 3])]
      (is (monoid? monoid)))))

(deftest list-monad-test
  (testing "List is a lawful monad"
    (extend-type List
      Monad
      (>>= [mv f]
        (if (seq (:wrapped mv))
          #_(List. []) (mconcat (map f (:wrapped mv)))
          (List. []))))
    (defmethod return List [_ v]
      (List. [v]))
    (let [mv (List. [1 2 3])
          f #(List. [(inc %)])
          g #(List. [(* 2 %)])
          x 1]
      (is (monad? mv f g x)))))
