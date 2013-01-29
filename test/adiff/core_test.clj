(ns adiff.core-test
  (:use clojure.test
        adiff.core))

(deftest singles
  (testing "Single items"
    (is (= (compose [(unq :D)] [:y]) []))
    (is (= (compose [:y] [])  [:y]))
    (is (= (compose [] [(unq :D)]) [(unq :D)]))
    (is (= (compose [(unq :I)] [:y]) [:y]))
    (is (= (compose [(unq :I)] [(unq :I)]) [(unq :I)]))
    (is (= (compose [(unq :D)] [(unq :I)]) [(unq :D)]))
))

(deftest lists
  (testing "compatible lists"
    (is (= (compose [] []) []))
    (is (= (compose [(unq :D) (unq :D) (unq :I) (unq :I) :g] [:a :b :c :d]) [:c :d :g]))
    (is (= (compose [(unq :D) (unq :I) (unq :D) :f] [(unq :D) (unq :D) (unq :I) (unq :I) :g]) [(unq :D) (unq :D) (unq :D) (unq :I) :f]))
    (is (= (compose [(unq :D) (unq :D) (unq :D) (unq :I) :f] [:a :b :c :d]) [:d :f]))
    (is (= (compose [(unq :D) (unq :I) (unq :D) :f] [:c :d :g]) [:d :f]))
    (is (= (compose [(unq :I) (unq :I) :x :y :z] [(unq :D) (unq :D) (unq :D) (unq :D) (unq :I) (unq :I)]) [(unq :D) (unq :D) (unq :D) (unq :D) (unq :I) (unq :I) :x :y :z]))
    (is (= (compose [:y] [(unq :D)]) [(unq :D) :y]))
    (is (= (compose [(unq :D) (unq :D) (unq :D) (unq :D) (unq :I) (unq :I) :x :y :z] [:a :b :c :d :e :f]) [:e :f :x :y :z])))
  (testing "incompatible lists"
    (is (thrown? UnsupportedOperationException (compose [(unq :D)] [])))
    (is (thrown? UnsupportedOperationException (compose [(unq :I) (unq :I) :x] [:a :b :c] )))
    (is (thrown? UnsupportedOperationException (compose [(unq :D) (unq :I) (unq :D) (unq :I)] [(unq :D) (unq :I) (unq :D) (unq :I)] )))
    (is (thrown? UnsupportedOperationException (compose [(unq :D)] [(unq :D)] )))
    (is (thrown? UnsupportedOperationException (compose [:a :b :c] [:x :I :y] )))
  )
)

(deftest dimensions
  (testing "item dimensions"
    (is (= (write-dimension (unq :D)) 0))
    (is (= (write-dimension (unq :I)) 1))
    (is (= (write-dimension :z) 1))
    (is (= (read-dimension (unq :D)) 1))
    (is (= (read-dimension (unq :I)) 1))
    (is (= (read-dimension :z) 0)))
  (testing "list dimension"
    (is (= (dimension [:a :b :c]) [3 0]))
    (is (= (dimension [:a (unq :I) :c]) [3 1]))
    (is (= (dimension [:a (unq :D) (unq :I)]) [2 2]))
    (is (= (dimension [(unq :I) (unq :D) :a :b (unq :D)]) [3 3]))
    (is (= (dimension [(unq :D) (unq :D) (unq :I) (unq :I) :f]) [3 4]))
    (is (= (dimension [(unq :D) (unq :D) (unq :D) (unq :D) (unq :I) (unq :I) :x :y :z]) [5 6])))
)
