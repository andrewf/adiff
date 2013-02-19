(ns adiff.core)

(defrecord reader
  ;"create a list item with special behavior, eg it reads from input"
  [type      ; either :scalar or :vector
   inside]   ; generally :I or :D, maybe a list
)

(def %D (reader. :scalar :D))
(def %I (reader. :scalar :I))

(defn reader?
  [item]
  (instance? reader item))

(defn delete?
  [item]
  (and (reader? item) (= (:inside item) :D)))

(defn keep?
  [item]
  (and (reader? item) (= (:inside item) :I)))

(defrecord stream-dimension
  ;"A write-read pair for a sequence of scalar patch elements"
  [write read])

(defn patch-dimension
  "a patch dimension is a vector tagged with patch-dimension meta attr"
  [& args]
  (with-meta (vec args) {:patch-dimension true}))

(defn stream
  [write read]
  (stream-dimension. write read))

(defn write-dimension
  [item]
  ; reader D is really the only thing with a w of 0
  (if (and (reader? item) (= (:inside item) :D)) 0 1))

(defn read-dimension
  [item]
  ; only readers can read
  (if (reader? item) 1 0))

(defn patch
  "a patch is a vector tagged with the patch meta attr"
  [& elements]
  (with-meta (vec elements) {:patch true}))

(defn patch?
  [item]
  (= (:patch (meta item)) true))

(defn dimension
  "Return patch-dimension of patch"
  [patch]
  (if (empty? patch)
    (patch-dimension (stream 0 0))
    (reduce
      (fn [sofar nextup]
        ; if nextup is a patch, add its dimension
        ; if nextup is a scalar, make a new stream-dimension
        ;    or add to an existing one
        (if (patch? nextup)
           (conj sofar (dimension nextup))
           ; nextup is a scalar
           (let [new-write (write-dimension nextup)
                 new-read  (read-dimension nextup)
                 last-index (- (count sofar) 1)  ; index of tailmost element
                 last-item (get sofar last-index)] ; actual tailmost element
             (if (instance? stream-dimension last-item)
               (let [old-write (:write last-item)
                     old-read (:read last-item)]
                 ; add the new write and read values to the last stream dimension
                 (assoc sofar last-index (stream (+ old-write new-write)
                                                 (+ old-read new-read))))
               ; have to make a new stream-dimension
               (conj sofar (stream new-write new-read))))))
      (patch-dimension )
      patch)))

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
    (not (= (:read  (get (dimension lhs) 0))
            (:write (get (dimension rhs) 0))))
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

