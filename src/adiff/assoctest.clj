; tests all *.edn files in directory specified as cmdline arg
; for associativity correctness. Ok, maybe not
; run like `lein run -m adiff.assoctest testdata'

(ns adiff.assoctest
  (:use adiff.core)
  (:require [clojure.edn :as edn])
  (:import java.io.File)
  (:import java.io.FileReader)
  (:import java.io.PushbackReader))


(defn readfile
  "java.io.File -> clojure data structures, all of them"
  [f]
  (let [pbr (PushbackReader. (FileReader. f))
        end (Object.) ]
    (take-while #(not= % eof) (repeatedly #(edn/read {:eof end} pbr)))))


(defn testthem
  "compose three patches two ways and check that the results are equal"
  [a b c]
  (let [left-assoc (compose (compose a b) c)
        right-assoc (compose a (compose b c))]
    (if (not (= left-assoc right-assoc))
      (println "fail")
      (println "pass"))))

(defn sugarify
  "make sure that %D, %I, (% ...) do the right thing"
  [patches]
  (let [mapper (fn [item]
                 (cond
                   (coll? item)
                     (if (= (first item) '%)
                       (% (sugarify (second item)))
                       (sugarify item))
                   (= item '%D) %D
                   (= item '%I) %I
                   :else item))]
    (map mapper patches)))
 
(defn maybe-compose
  "either compose them and return [composed, nil] or, if there's an error, return [nil, exception]"
  [a b]
  (try
    [(compose a b) nil]
    (catch UnsupportedOperationException e
      [nil e])))
  

(defn test-three
  [a b result]
  (let [[found  e] (maybe-compose a b)]
    (if (not= found nil)
      (if (not= found result)
        (println (str "failed " found " != " result)))
      (println (str "failed to do compose: " e)))))

(defn test-four
  [a b c result]
  (let [[ab ab-err] (maybe-compose a b)
        [bc bc-err] (maybe-compose b c)]
    (if (not (nil? ab))
      (if (not (nil? bc))
        ; try the next layer of compositions
        (let [[ab-c ab-c-err] (maybe-compose ab c)
              [a-bc a-bc-err] (maybe-compose a bc)]
          (if (not (nil? ab-c))
            (if (not (nil? a-bc))
              (do
                (if (not= ab-c result)
                  (println (str "left-association is wrong " ab-c " != " result)))
                (if (not= a-bc result)
                  (println (str "right-association is wrong " a-bc " != " result))))
              (println (str "right-association failed: " a-bc-err)))
            (println (str "left-association failed: " ab-c-err))))
        (println (str "right composition failed: " bc-err)))
      (println (str "left composition failed: " ab-err)))))

(defn testfile
  "check the first two or three items, composed, against the last item in the file"
  [f]
  (let [[a b c d] (sugarify (readfile f))]
    ; if d is nil, no assoc test, just a*b=c
    (if (= d nil)
      (test-three a b c)
      (test-four a b c d))))

(defn -main [dirname]
  (doseq [f (.listFiles (File. dirname))]
    (let [fname (.getName f)]
      (if (.endsWith fname ".edn")
        (do
          (println (str "reading " (.getName f)))
          (testfile f))))
  ))
