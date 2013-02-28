(ns adiff.core-test
  (:use clojure.test
        adiff.core))


(deftest singles
  (testing "Single items"
    (is (= (compose [%D] [:y]) []))
    (is (= (compose [:y] []) [ :y]))
    (is (= (compose [] [ %D]) [ %D]))
    (is (= (compose [ %I] [ :y]) [ :y]))
    (is (= (compose [ %I] [ %I]) [ %I]))
    (is (= (compose [ %D] [ %I]) [ %D]))
    (is (= (compose [ %D] [ :I]) [ ]))
    (is (= (compose [ :I %I] [ "42"]) [ :I "42"]))
))

(deftest lists
  (testing "compatible lists"
    (is (= (compose [] []) []))

    (is (= (compose [ %D %D %I %I :g]
                    [ :a :b :c :d])
           [ :c :d :g]))

    (is (= (compose [ %D %I %D :f]
                    [ %D %D %I %I :g])
           [ %D %D %D %I :f]))

    (is (= (compose [ %D %D %D %I :f]
                    [ :a :b :c :d])
           [ :d :f]))

    (is (= (compose [ %D %I %D :f]
                    [ :c :d :g])
           [ :d :f]))

    (is (= (compose [ %I %I :x :y :z]
                    [ %D %D %D %D %I %I])
           [ %D %D %D %D %I %I :x :y :z]))

    (is (= (compose [ :y]
                    [ %D])
           [ %D :y]))

    (is (= (compose [ %D %D %D %D %I %I :x :y :z]
                    [ :a :b :c :d :e :f])
           [ :e :f :x :y :z]))

    (is (= (compose [ %D :x %I (% [ :y %I %D]) %I %I %D]
                    [ :a :b [ :c :d] :e :f :g])
           [ :x :b [ :y :c] :e :f]))
    
    (is (= (compose [ [ :y %D] (% [ %D %D :x :z]) %D :w]
                    [ [ :a :b] :c])
           [ [ :y %D] [ :x :z] :w]))

    (is (= (compose [ %D [ :a :b :x] %I]
                    [ [ :a :b :c] [ :d :e]])
           [ [ :a :b :x] [ :d :e]]))

    (is (= (compose [ (% [ %I %I %D :x]) %I]
                    [ [ :a :b :c] [ :d :e]])
           [ [ :a :b :x] [ :d :e]]))
  )

  (testing "incompatible lists"
    (is (thrown? UnsupportedOperationException (compose [ %D] [ ])))
    (is (thrown? UnsupportedOperationException (compose [ %I %I :x] [ :a :b :c] )))
    (is (thrown? UnsupportedOperationException (compose [ %D %I %D %I] [ %D %I %D %I] )))
    (is (thrown? UnsupportedOperationException (compose [ %D] [ %D] )))
    (is (thrown? UnsupportedOperationException (compose [ :a :b :c] [ :x :I :y] )))
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
    (is (= (dimension [ :a :b :c]) [(stream 3 0)]))
    (is (= (dimension [ :a %I :c]) [(stream 3 1)]))
    (is (= (dimension [ :a %D %I]) [(stream 2 2)]))
    (is (= (dimension [ %I %D :a :b %D]) [(stream 3 3)]))
    (is (= (dimension [ %D %D %I %I :f]) [(stream 3 4)]))
    (is (= (dimension [ %D %D %D %D %I %I :x :y :z]) [(stream 5 6)]))
    (is (= (dimension [ %D %I (% [ :foo %I %D]) :x %I]) [(stream 1 2) (patch-dimension (stream 2 2)) (stream 2 1)]))
    (is (= (dimension [ %D %I [ :foo %I %D] :x %I]) [(stream 4 3)])))
)
