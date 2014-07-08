# funk [![Build Status](https://travis-ci.org/txus/funk.png)](https://travis-ci.org/txus/funk)

An implementation of functors, applicative functors and monads on top of
Clojure records, protocols and multimethods.

Funk makes it easy to verify their laws at runtime (e.g. in tests).

Inspired by the great tutorial by Leonardo Borges: [Monads in small bites][tutorial]. If you're not familiar with the topic, read it before continuing with this Readme.

## Usage

Add this to your `project.clj` dependencies:

```clojure
[funk "0.1.0-SNAPSHOT"]
```

### Implementing the Maybe Monad with funk

Classic. Here's the code:

```clojure
(ns example.maybe
  (:require [funk.core :refer :all]))

(defrecord Maybe [value]
  Monad
  (>>= [mv f]
    (if (:value mv)
      (f (:value mv))
      (Maybe. nil))))

(defmethod return Maybe [_ v]
  (Maybe. v))
```

Now let's verify that it satisfies the three monad laws (right unit, left unit and associativity) with `funk.verify/monad?`:

```clojure
(ns example.maybe-test
  (:require [clojure.test :refer :all]
            [funk.verify :refer [monad?]]
            [example.maybe :refer :all])
  (:import [example.maybe Maybe]))

(deftest maybe-monad-test
  (testing "Maybe is a lawful monad"
    (let [mv (Maybe. 10)
          f #(Maybe. (inc %))
          g #(Maybe. (* 2 %))
          x 10]
      (is (monad? mv f g x)))))
```

That was easy! Now for something more complicated.

### Implementing the List functor, applicative functor, monoid and monad with funk

#### Implementing the List functor

Let's start with the List functor. We just need to implement the `fmap`
function of the `Functor` protocol.

```clojure
(ns example.list
  (:require [funk.core :refer :all]))

(defrecord List [wrapped]
  Functor
  (fmap [functor f]
    (List. (map f (:wrapped functor)))))
```

Now let's verify it is a lawful functor with `funk.verify/functor?`, which
makes sure our functor satisfies both the identity and composition laws:

```clojure
(ns example.list-test
  (:require [clojure.test :refer :all]
            [funk.verify :refer :all]
            [example.list :refer :all])
  (:import [example.list List]))

(deftest list-functor-test
  (testing "List is a lawful functor"
    (let [functor (List. [1 2 3])
          f inc
          g (partial * 2)]
      (is (functor? functor f g)))))
```

#### Implementing the List applicative functor

Let's augment our List record to be an applicative functor as well. To do that
we need to implement the `pure` and `<*>` multimethods for List:

```clojure
; in our namespace example.list

(defmethod pure List [_ v]
  (List. [v]))

(defmethod <*> List [fs xs]
  (List. (for [f (:wrapped fs)
               x (:wrapped xs)]
           (f x))))
```

And now let's verify it is a lawful applicative functor (satisfying identity,
composition, homomorphism and interchange) with
`funk.verify/applicative-functor?`:

```clojure
; in our namespace example.list-test

(deftest list-applicative-functor-test
  (testing "List is a lawful applicative functor"
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
```

#### Implementing the List monoid

Making our List a lawful monoid under concatenation is no sweat with funk. We just need to implement `mempty` and `mappend` from the `Monoid` protocol:

```clojure
; in our namespace example.list

(extend-type List
  Monoid
  (mempty [_] (List. []))
  (mappend [x y] (List. (concat (:wrapped x) (:wrapped y)))))
```

And now let's verify that it is a lawful monoid with `funk.verify/monoid?`,
that is, it satisfies the identity and the associativity laws:

```clojure
; in our namespace example.list-test

(deftest list-monoid-test
  (testing "List is a lawful monoid"
    (let [monoid (List. [1 2 3])]
      (is (monoid? monoid)))))
```

That was really easy!

#### Implementing the List monad

Now for some more action. Making our List a monad turns out to be pretty
simple as well -- we just need to implement `>>=` from the `Monad` protocol
and the multimethod `return` for our List.

```clojure
; in our namespace example.list

(extend-type List
  Monad
  (>>= [mv f]
    (if (seq (:wrapped mv))
      #_(List. []) (mconcat (map f (:wrapped mv)))
      (List. []))))

(defmethod return List [_ v]
  (List. [v]))
```

To verify that List is a lawful monad that satisfies the right unit, left unit
and associativity laws, we can use `funk.verify/monad?`:

```clojure
; in our namespace example.list-test

(deftest list-monad-test
  (testing "List is a lawful monad"
    (let [mv (List. [1 2 3])
          f #(List. [(inc %)])
          g #(List. [(* 2 %)])
          x 1]
      (is (monad? mv f g x)))))
```

Done! Our List is a functor, applicative functor, monoid and monad, all
verified by our tests!

## License

Copyright © 2014 Josep M. Bach

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[tutorial]: http://www.leonardoborges.com/writings/2012/11/30/monads-in-small-bites-part-i-functors/
