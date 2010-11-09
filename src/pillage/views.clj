(ns pillage.views)

(defn html-doc
  "Base template for generating an HTML document"
  ([] (html-doc "Pillage RSS" nil))
  ([title & body] (str "<html><head><title>" title "</title></head><body><h1>Hello World!</h1></body></html>")))
