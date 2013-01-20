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
    ;(is (= (compose '(D) '(I)) '(D)))
))
