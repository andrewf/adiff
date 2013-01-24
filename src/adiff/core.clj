(ns adiff.core)


(defn write-dimension
  [item]
  ; D is really the only thing with a w of 0
  (if (= item :D) 0 1))

(defn read-dimension
  [item]
  ; D and I read, everything else just inserts
  (if (or (= item :I) (= item :D)) 1 0))


(defn dimension
  "return [write, read] dimensions"
  [patch]
  [(apply + (map write-dimension patch)) (apply + (map read-dimension patch))])


(defn compose
  "Process next glyphs from RHS and LHS, and return resulting patch. LHS is patch, RHS is source"
  [lhs rhs]
  (cond
    
    ; read dimension of lhs must = write dimension of rhs
    (not (= ((dimension lhs) 1)
            ((dimension rhs) 0)))
      (throw (UnsupportedOperationException.
              "Cannot compose patches with incompatible dimension"))

    ; terminate if either list is empty
    (and (empty? lhs) (empty? rhs)) '()

    ; propagate write-0 items from source to output
    (= (write-dimension (first rhs)) 0) (cons (first rhs) (compose lhs (rest rhs)))
    
    ; insert read-0 items from patch (lhs) to output
    (= (read-dimension (first lhs)) 0) (cons (first lhs) (compose (rest lhs) rhs))

    ; apply D from top/lhs. D.y = <>, D.I = D
    (= (first lhs) :D)
      (if (= (first rhs) :I)
        (cons :D (compose (rest lhs) (rest rhs)))
        (compose (rest lhs) (rest rhs)))

    ; I in lhs copies value
    (= (first lhs) :I) (cons (first rhs) (compose (rest lhs) (rest rhs)))

    ; other stuff in lhs is inserted. TODO better error type/msg
    :else (throw (UnsupportedOperationException. "Invalid token or something"))
))

