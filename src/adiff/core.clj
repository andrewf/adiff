(ns adiff.core)

(defn compose
  "Process next glyphs from RHS and LHS, and return resulting patch. LHS is patch, RHS is source"
  [lhs rhs]
  (cond

    ; terminate if either list is empty
    (and (empty? lhs) (empty? rhs)) (do (println "terminate") '())

    ; propagate D from bottom to top, source to output
    (= (first rhs) 'D) (cons 'D (compose lhs (rest rhs)))

    ; apply D from top/lhs. D.y = <>, D.I = D
    (= (first lhs) 'D)
      (if (= (first rhs) 'I)
        (cons 'D (compose (rest lhs) (rest rhs)))
        (compose (rest lhs) (rest rhs)))

    ; I in lhs copies value
    (= (first lhs) 'I) (cons (first rhs) (compose (rest lhs) (rest rhs)))

    ; other stuff in lhs is inserted
    :else (do
      (println "insert")
      (println (first lhs))
      (println (rest lhs))
      (println rhs)
      (cons (first lhs) (compose (rest lhs) rhs))) ))

(defn write-dimension
  [item]
  ; D is really the only thing with a w of 0
  (if (= item 'D) 0 1))

(defn read-dimension
  [item]
  ; D and I read, everything else just inserts
  (if (or (= item 'I) (= item 'D)) 1 0))


(defn dimension
  "return [write, read] dimensions"
  [patch]
  [(apply + (map write-dimension patch)) (apply + (map read-dimension patch))])
