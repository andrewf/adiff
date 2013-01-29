(ns adiff.core-test
  (:use clojure.test
        adiff.core))

(deftest singles
  (testing "Single items"
    (is (= (compose [%D] [:y]) []))
    (is (= (compose [:y] [])  [:y]))
    (is (= (compose [] [%D]) [%D]))
    (is (= (compose [%I] [:y]) [:y]))
    (is (= (compose [%I] [%I]) [%I]))
    (is (= (compose [%D] [%I]) [%D]))
))

(deftest lists
  (testing "compatible lists"
    (is (= (compose [] []) []))
    (is (= (compose [%D %D %I %I :g] [:a :b :c :d]) [:c :d :g]))
    (is (= (compose [%D %I %D :f] [%D %D %I %I :g]) [%D %D %D %I :f]))
    (is (= (compose [%D %D %D %I :f] [:a :b :c :d]) [:d :f]))
    (is (= (compose [%D %I %D :f] [:c :d :g]) [:d :f]))
    (is (= (compose [%I %I :x :y :z] [%D %D %D %D %I %I]) [%D %D %D %D %I %I :x :y :z]))
    (is (= (compose [:y] [%D]) [%D :y]))
    (is (= (compose [%D %D %D %D %I %I :x :y :z] [:a :b :c :d :e :f]) [:e :f :x :y :z])))
  (testing "incompatible lists"
    (is (thrown? UnsupportedOperationException (compose [%D] [])))
    (is (thrown? UnsupportedOperationException (compose [%I %I :x] [:a :b :c] )))
    (is (thrown? UnsupportedOperationException (compose [%D %I %D %I] [%D %I %D %I] )))
    (is (thrown? UnsupportedOperationException (compose [%D] [%D] )))
    (is (thrown? UnsupportedOperationException (compose [:a :b :c] [:x :I :y] )))
  )
)

(deftest dimensions
  (testing "item dimensions"
    (is (= (write-dimension %D) 0))
    (is (= (write-dimension %I) 1))
    (is (= (write-dimension :z) 1))
    (is (= (read-dimension %D) 1))
    (is (= (read-dimension %I) 1))
    (is (= (read-dimension :z) 0)))
  (testing "list dimension"
    (is (= (dimension [:a :b :c]) [3 0]))
    (is (= (dimension [:a %I :c]) [3 1]))
    (is (= (dimension [:a %D %I]) [2 2]))
    (is (= (dimension [%I %D :a :b %D]) [3 3]))
    (is (= (dimension [%D %D %I %I :f]) [3 4]))
    (is (= (dimension [%D %D %D %D %I %I :x :y :z]) [5 6])))
)
