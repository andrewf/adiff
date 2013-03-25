; tests all *.edn files in directory specified as cmdline arg
; for associativity correctness. Ok, maybe not
(ns adiff.assoctest
  (:use adiff.core)
  (:import java.io.File)
  (:import java.io.FileReader)
  (:import java.io.PushbackReader))

(def testdata
  [
    [[] [] []]
  ])

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

(defn -main [dirname]
  (println "hello world")
  (doseq [f (.listFiles (File. dirname))]
    (println (str "reading " (.getName f)))
    (println (readfile f))
  )
)

  ;(testthem [%D :y (% [%I %D :x %I]) %I]
  ;          [%I (% [:a :b %I %D]) (% [%I %I 3])]
  ;          [ [:Q :R :S] [:T :U] [:V :W]])
