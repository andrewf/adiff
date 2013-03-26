; tests all *.edn files in directory specified as cmdline arg
; for associativity correctness. Ok, maybe not
(ns adiff.assoctest
  (:use adiff.core)
  (:import java.io.File)
  (:import java.io.FileReader)
  (:import java.io.PushbackReader))


(defn readfile
  "java.io.File -> clojure data structures, all of them"
  [f]
  (let [pbr (PushbackReader. (FileReader. f))
        eof (Object.) ]
    (take-while #(not= % eof) (repeatedly #(read pbr false eof)))))


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
                   (coll? item) (sugarify item)
                   (= item '%D) %D
                   (= item '%I) %I
                   :else item))]
    (map mapper patches)))
 
(defn test-three
  [a b result]
  (if-let [found (try
                 (compose a b)
                 (catch UnsupportedOperationException e
                   (println (str "failed to compose: " e))
                   nil))]
    (if (not= found result)
      (println (str found " != " result))
      (println "success"))))

(defn test-four
  [a b c result]
  (println "haha, just kidding"))

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

  ;(testthem [%D :y (% [%I %D :x %I]) %I]
  ;          [%I (% [:a :b %I %D]) (% [%I %I 3])]
  ;          [ [:Q :R :S] [:T :U] [:V :W]])
