(ns adiff.core)

(defrecord reader
  ;"create a list item with special behavior, eg it reads from input"
  [inside]
)

(def %D (reader. :D))
(def %I (reader. :I))

(defn reader?
  [item]
  (instance? reader item))

(defn delete?
  [item]
  (and (reader? item) (= (:inside item) :D)))

(defn keep?
  [item]
  (and (reader? item) (= (:inside item) :I)))

(defn write-dimension
  [item]
  ; reader D is really the only thing with a w of 0
  (if (and (reader? item) (= (:inside item) :D)) 0 1))

(defn read-dimension
  [item]
  ; only readers can read
  (if (reader? item) 1 0))


(defn dimension
  "return [write, read] dimensions"
  [patch]
  [(apply + (map write-dimension patch)) (apply + (map read-dimension patch))])


(defn compose-single
  "compose a read-1 lhs and write-1 rhs. nil means don't add anything"
  [lhs rhs]
  (assert (reader? lhs)) ; otherwise wouldn't be a reader, and composing is invalid
  (cond
    (delete? lhs)
      (if (reader? rhs)
        %D   ; :D has to delete what rhs would have read
        nil)  ; D*y = []
    (keep? lhs) rhs
  )
)


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

    ; the two head elements are read-1 and write-1, composable
    ; compose them, and cons the (optional) result with the composition
    ; of the rest of the list. Be sure to handle empty cases
    :else
      (let [tail (compose (rest lhs) (rest rhs))]
        (if-let [front (compose-single (first lhs) (first rhs))]
          (cons front tail)
          tail))
))

