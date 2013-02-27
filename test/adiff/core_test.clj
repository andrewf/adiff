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
           (patch :e :f :x :y :z)))

    (is (= (compose (patch %D :x %I (% (patch :y %I %D)) %I %I %D)
                    (patch :a :b (patch :c :d) :e :f :g))
           (patch :x :b (patch :y :c) :e :f)))
    
    (is (= (compose (patch (patch :y %D) (% (patch %D %D :x :z)) %D :w)
                    (patch (patch :a :b) :c))
           (patch (patch :y %D) (patch :x :z) :w)))
  )

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
    (is (= (dimension (patch :a :b :c)) [(stream 3 0)]))
    (is (= (dimension (patch :a %I :c)) [(stream 3 1)]))
    (is (= (dimension (patch :a %D %I)) [(stream 2 2)]))
    (is (= (dimension (patch %I %D :a :b %D)) [(stream 3 3)]))
    (is (= (dimension (patch %D %D %I %I :f)) [(stream 3 4)]))
    (is (= (dimension (patch %D %D %D %D %I %I :x :y :z)) [(stream 5 6)]))
    (is (= (dimension (patch %D %I (% (patch :foo %I %D)) :x %I)) [(stream 1 2) (patch-dimension (stream 2 2)) (stream 2 1)]))
    (is (= (dimension (patch %D %I (patch :foo %I %D) :x %I)) [(stream 4 3)])))
)
