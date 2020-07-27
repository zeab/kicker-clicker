
//Imports
import Common._
import Versions._
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._
import sbt.Def

object Docker {

  val repo: Option[String] = Some("zeab")

  //Image List
  val I = new {
    val openjdk8Alpine: String = "openjdk:8-jdk-alpine"
    val openjdk8Slim: String = "openjdk:8-jdk-slim"
    val headlessFirefox: String = "selenium/standalone-firefox:3.141.59-20200525"
  }

  //docker run -d -p 4444:4444 --shm-size 2g selenium/standalone-firefox:3.141.59-20200525

  val rootDockerSettings: Seq[Def.Setting[_]] = Seq(
    dockerBaseImage := I.openjdk8Alpine,
    dockerRepository := repo,
    dockerLabels := mapDockerLabels("kickerclicker", rootVersion, buildTime),
    dockerUpdateLatest := true
  )

}
