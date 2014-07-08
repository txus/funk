(ns funk.verify
  (:require [funk.core :refer :all]))

(defn functor? [functor f g]
  (and
    ; identity
    (= (fmap functor identity)
       (identity functor))
    ; composition
    (= (fmap functor (comp f g))
       (-> functor (fmap g) (fmap f)))))

(defn applicative-functor? [functor f g x]
  (let [instance (class functor)]
    (and
      ; identity
      (= (<*> (pure instance f) functor)
         (fmap functor f))
      ; composition
      #_(let [fu (pure instance f)
            fv (pure instance g)]
        (= (-> (pure instance (fn [x] (partial comp x)))
               (<*> fu)
               (<*> fv)
               (<*> functor))
           (<*> fu (<*> fv functor))))
      ; homomorphism
      (= (-> (pure instance f)
             (<*> (pure instance x)))
         (pure instance (f x)))
      ; interchange
      (let [fu (pure instance f)]
        (= (<*> fu (pure instance x))
           (<*> (pure instance #(% x)) fu))))))

(defn monoid? [monoid]
  (and
    ; identity
    (= (mappend monoid (mempty monoid))
       (mappend (mempty monoid) monoid)
       monoid)
    ; associativity
    (= (mappend monoid (mappend monoid monoid))
       (mappend (mappend monoid monoid) monoid))))

(defn monad? [mv f g x]
  (let [ret (partial return (class mv))]
    (and
      ; right unit
      (= (>>= mv ret)
         mv)
      ; left unit
      (= (>>= (ret x) f)
         (f x))
      ; associativity
      (= (>>= (>>= mv f) g)
         (>>= mv (fn [y] (>>= (f y) g)))))))
