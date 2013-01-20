(ns adiff.core-test
  (:use clojure.test
        adiff.core))

(deftest singles
  (testing "Single items"
    (is (= (compose '(D) '(y)) '()))
    (is (= (compose '(y) '())  '(y)))
    (is (= (compose '()  '(D)) '(D)))
    (is (= (compose '(I) '(y)) '(y)))
    (is (= (compose '(I) '(I)) '(I)))
    (is (= (compose '(D) '(I)) '(D)))
))

(deftest dimensions
  (testing "item dimensions"
    (is (= (write-dimension 'D) 0))
    (is (= (write-dimension 'I) 1))
    (is (= (write-dimension 'z) 1))
    (is (= (read-dimension 'D) 1))
    (is (= (read-dimension 'I) 1))
    (is (= (read-dimension 'z) 0)))
  (testing "list dimension"
    (is (= (dimension '(a b c)) [3 0]))
    (is (= (dimension '(a I c)) [3 1]))
    (is (= (dimension '(a D I)) [2 2]))
    (is (= (dimension '(I D a b D)) [3 3]))
    (is (= (dimension '(D D I I f)) [3 4]))
))
