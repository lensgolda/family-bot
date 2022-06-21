(defproject com.github.lensgolda/family-bot "0.1.0-SNAPSHOT"
  :description "Telegram family bot"
  :url "https://github.com/lensgolda/family-bot/"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}
  :min-lein-version "2.8.1"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/core.async "1.5.648"]
                 [org.clojure/data.zip "1.0.0"]
                 [org.clojure/data.json "2.4.0"]
                 [org.clojure/tools.logging "1.2.4"]
                 [clojure.java-time "0.3.3"]
                 [metosin/jsonista "0.3.5"]
                 [org.immutant/scheduling "2.1.10"]
                 [http-kit "2.6.0"]
                 [integrant "0.8.0"]
                 [aero "1.1.6"]
                 [org.apache.logging.log4j/log4j-core "2.17.2"]
                 [org.apache.logging.log4j/log4j-api "2.17.2"]]
  :repositories [["sonatype" {:url "https://oss.sonatype.org/content/repositories/releases"
                              :snapshots false
                              :releases {:checksum :warn :update :never}}]]

  :profiles {:debug {:debug true
                     :injections [(prn (into {} (System/getProperties)))]}
             :dev {:resource-paths ["dev"]
                   :dependencies [[integrant/repl "0.3.2"]]}
             ;; activated automatically during uberjar
             :uberjar {:aot :all}}
  :source-paths ["src"]
  :test-paths ["test"]
  :resource-paths ["resources"] ; Non-code files included in classpath/jar.
  :main ^{:skip-aot true} family.bot.core
  :jvm-opts ["-Dlog4j2.formatMsgNoLookups=true"
             "-Dclojure.tools.logging.factory=clojure.tools.logging.impl/log4j2-factory"
             "-Dlog4j.configurationFile=resources/log4j2.properties"]
  :jar-name "family-bot.jar"
  ;; As above, but for uberjar.
  :uberjar-name "family-bot-standalone.jar"
  :clean-targets [:target-path :compile-path]
  :deploy-branches ["heroku-deploy"]
  ;; These settings disable the implicit loading of middleware and
  ;; hooks, respectively. You can disable both with :implicits false.
  :implicit-middleware false
  :implicit-hooks false)
