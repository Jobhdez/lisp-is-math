(defproject lisp-is-math "0.1.0-SNAPSHOT"
  :description "A small program that uses macros to abstract a common pattern."
  :license {:name "GPL-2.0"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                  [compojure "1.7.0"]
                 [http-kit "2.3.0"]
                 [ring/ring-defaults "0.3.4"]
                 [org.clojure/data.json "2.4.0"]
                 [cheshire "5.13.0"]
                 [com.github.seancorfield/next.jdbc "1.3.834"]
                 [org.postgresql/postgresql "42.2.10"]
                 [com.github.seancorfield/honeysql "2.3.928"]]
  :repl-options {:init-ns lisp-is-math.core}
  :main ^:skip-aot lisp-is-math.core)
