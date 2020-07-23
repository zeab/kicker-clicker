//Imports
import sbt._

object Dependencies {

  //List of Versions
  val V = new {
    val scalaTest                   = "3.0.5"
    val javaNetHttpClientFull       = "2.0.1"
    val mySql                       = "8.0.13"
    val sel                         = "3.141.59"

    val akka                        = "2.5.22"
    val akkaHttp                    = "10.1.8"
    val akkaHttpCirce               = "1.25.2"
    val akkaKafka                   = "1.0.1"
    val circe                       = "0.11.1"
    val logbackJson                 = "5.2"
    val logback                     = "1.2.3"

    val scalaXML                    = "1.0.6"
    val datastax                    = "3.4.0"
    val zooKeeper                   = "3.4.14"
    val aenea                       = "1.0.0"
  }

  //List of Dependencies
  val D = new {
    val mySql                       = "mysql" % "mysql-connector-java" % V.mySql
    //Selenium
    val sel                         = "org.seleniumhq.selenium" % "selenium-java" % V.sel
    val selF                        = "org.seleniumhq.selenium" % "selenium-firefox-driver" % V.sel
    //Akka
    val akkaStream                  = "com.typesafe.akka" %% "akka-stream" % V.akka
    //Akka Http
    val akkaHttp                    = "com.typesafe.akka" %% "akka-http" % V.akkaHttp
    //Json
    val circeCore                   = "io.circe" %% "circe-parser" % V.circe
    val circeParser                 = "io.circe" %% "circe-generic" % V.circe
    val akkaHttpCirce               = "de.heikoseeberger" %% "akka-http-circe" % V.akkaHttpCirce
    //Logging
    val akkaSlf4j                   = "com.typesafe.akka" %% "akka-slf4j" % V.akka
    val logback                     = "ch.qos.logback" % "logback-classic" % V.logback
    val logbackJson                 = "net.logstash.logback" % "logstash-logback-encoder" % V.logbackJson
    //Test
    val scalaTest                   = "org.scalatest" %% "scalatest" % V.scalaTest % "test"
  }

  val rootDependencies: Seq[ModuleID] = Seq(
    D.mySql,
    D.akkaStream,
    D.sel,
    D.akkaHttp,
    D.circeCore,
    D.circeParser,
    D.akkaHttpCirce,
    D.scalaTest
  )

}
