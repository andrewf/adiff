(ns adiff.core)

(defn compose
  "Process next glyphs from RHS and LHS, and return resulting patch. LHS is patch, RHS is source"
  [lhs rhs]
  (cond

    ; terminate if either list is empty
    (and (empty? lhs) (empty? rhs)) (do (println "terminate") '())

    ; propagate D from bottom to top, source to output
    (= (first rhs) 'D) (cons 'D (compose lhs (rest rhs)))

    ; apply D from top/lhs
    (= (first lhs) 'D) (compose (rest lhs) (rest rhs))

    ; I in lhs copies value
    (= (first lhs) 'I) (cons (first rhs) (compose (rest lhs) (rest rhs)))

    ; other stuff in lhs is inserted
    :else (do
      (println "insert")
      (println (first lhs))
      (println (rest lhs))
      (println rhs)
      (cons (first lhs) (compose (rest lhs) rhs))) ))

