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

(defn vector-reader?
  [item]
  ; ought to be able to do this with short-circuit and
  (if (reader? item)
    (if (= (:type item) :vector)
      true
      false)
    false))

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

(defn ensure-patch
  "if vector is passed in, return patch version"
  [item]
  (if (vector? item)
    (with-meta item (assoc (meta item) :patch true))
    item))

(defn %
  "shortcut for making readers"
  [item]
  (if (vector? item)
    (reader. :vector (ensure-patch item))
    (reader. :scalar item)))

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
        (if (vector-reader? nextup)
           (conj sofar (dimension (:inside nextup)))
           ; nextup is a scalar, or close enough as makes no difference
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

(declare compose-single)

(defn compose
  "Process next glyphs from RHS and LHS, and return resulting patch. LHS is patch, RHS is source"
  [lhs rhs]
  (if-let [rhs0 (first rhs)]
    (if (= (write-dimension rhs0) 0)
      (cons rhs0 (compose lhs (rest rhs)))
      ; if rhs0 is a writer...
      (if-let [lhs0 (first lhs)]
        (if (reader? lhs0)
          ; compose the two head elements and cons result...
          (let [tail (compose (rest lhs) (rest rhs))]
            (if-let [front (compose-single lhs0 rhs0)]
              (cons front tail)
              tail))
          ; lhs reads 0, is inserted
          (cons lhs0 (compose (rest lhs) rhs)))
        ; lhs is empty, cannot write
        (throw (UnsupportedOperationException. "too many writers in RHS"))))
    ; rhs is empty
    (if-let [lhs0 (first lhs)]
      (if (reader? lhs0)
        (throw (UnsupportedOperationException. "too many readers in LHS"))
        ; lhs reads 0, is inserted
        (cons lhs0 (compose (rest lhs) rhs)))
      ; lhs is empty
      '()  ; all done, return empty list
)))

(defn compose-single
  "compose a read-1 lhs and write-1 rhs. nil means don't add anything"
  [lhs rhs]
  (assert (reader? lhs)) ; otherwise wouldn't be a reader, and composing is invalid
  (cond
    (vector-reader? lhs)
      (let [left-patch (:inside lhs)]
        (cond 
          (vector-reader? rhs)
            (compose left-patch (:inside rhs))
          (patch? rhs)
            (compose left-patch rhs)
          :else (throw (UnsupportedOperationException.
                        "tried to compose patch with scalar"))))
    (delete? lhs)
      (if (reader? rhs)
        %D   ; :D has to delete what rhs would have read
        nil)  ; D*y = []
    (keep? lhs) rhs
  )
)
