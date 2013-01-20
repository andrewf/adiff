(ns adiff.core-test
  (:use clojure.test
        adiff.core))

(deftest singles
  (testing "Single items"
    (is (= 0 1))))

(deftest associatvity
  (testing "associativity"
    (is (= 1 1))))
