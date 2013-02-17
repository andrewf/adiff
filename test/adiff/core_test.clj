(ns adiff.core-test
  (:use clojure.test
        adiff.core))

(deftest singles
  (testing "Single items"
    (is (= (compose (patch %D) (patch :y)) (patch)))
    (is (= (compose (patch :y) (patch)) (patch :y)))
    (is (= (compose (patch) (patch %D)) (patch %D)))
    (is (= (compose (patch %I) (patch :y)) (patch :y)))
    (is (= (compose (patch %I) (patch %I)) (patch %I)))
    (is (= (compose (patch %D) (patch %I)) (patch %D)))
    (is (= (compose (patch %D) (patch :I)) (patch )))
    (is (= (compose (patch :I %I) (patch "42")) (patch :I "42")))
))

(deftest lists
  (testing "compatible lists"
    (is (= (compose (patch) (patch)) (patch)))

    (is (= (compose (patch %D %D %I %I :g)
                    (patch :a :b :c :d))
           (patch :c :d :g)))

    (is (= (compose (patch %D %I %D :f)
                    (patch %D %D %I %I :g))
           (patch %D %D %D %I :f)))

    (is (= (compose (patch %D %D %D %I :f)
                    (patch :a :b :c :d))
           (patch :d :f)))

    (is (= (compose (patch %D %I %D :f)
                    (patch :c :d :g))
           (patch :d :f)))

    (is (= (compose (patch %I %I :x :y :z)
                    (patch %D %D %D %D %I %I))
           (patch %D %D %D %D %I %I :x :y :z)))

    (is (= (compose (patch :y)
                    (patch %D))
           (patch %D :y)))

    (is (= (compose (patch %D %D %D %D %I %I :x :y :z)
                    (patch :a :b :c :d :e :f))
           (patch :e :f :x :y :z))))

  (testing "incompatible lists"
    (is (thrown? UnsupportedOperationException (compose (patch %D) (patch ))))
    (is (thrown? UnsupportedOperationException (compose (patch %I %I :x) (patch :a :b :c) )))
    (is (thrown? UnsupportedOperationException (compose (patch %D %I %D %I) (patch %D %I %D %I) )))
    (is (thrown? UnsupportedOperationException (compose (patch %D) (patch %D) )))
    (is (thrown? UnsupportedOperationException (compose (patch :a :b :c) (patch :x :I :y) )))
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
    (is (= (dimension (patch :a :b :c)) [3 0]))
    (is (= (dimension (patch :a %I :c)) [3 1]))
    (is (= (dimension (patch :a %D %I)) [2 2]))
    (is (= (dimension (patch %I %D :a :b %D)) [3 3]))
    (is (= (dimension (patch %D %D %I %I :f)) [3 4]))
    (is (= (dimension (patch %D %D %D %D %I %I :x :y :z)) [5 6])))
)
