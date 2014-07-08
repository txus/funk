(ns funk.core)

(defprotocol Functor
  (fmap [functor f]))

(defmulti pure (fn [f _] f))
(defmulti <*> (fn [fs _] (class fs)))

(defprotocol Monoid
  (mempty [_])
  (mappend [x y]))

(defn mconcat [ms]
  (reduce mappend (mempty (first ms)) ms))

(defprotocol Monad
  (>>= [mv f]))

(defn then [ma mb]
  (>>= ma (fn [_] mb)))

(defmulti return (fn [f _] f))
