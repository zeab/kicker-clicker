package com.zeab.kickerclicker

import java.time.ZonedDateTime

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import com.zeab.kickerclicker.httpservice.Routes

import scala.collection.immutable
import scala.collection.immutable.Range.Inclusive
import scala.concurrent.ExecutionContext
import scala.io.StdIn

object KickerClicker extends App {

  //Set up stuff
  System.setProperty("webdriver.gecko.driver", System.getenv("DRIVER_LOCATION"))

  //Actor System Stuff
  implicit val system: ActorSystem = ActorSystem("Kicker-Clicker", ConfigFactory.load())
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  //val name = "womens-react-vision-pale-ivory"
  //val name = "zoom-double-stacked-barely-volt"
  //val name = "sb-dunk-low-pro-grateful-dead-opti-yellow"
  //val name = "womens-air-vapormax-2020-flyknit-pure-platinum"
  //val name = "air-force-1-drew-league"
  val name = "air-jordan-4-off-white-sail"
  val ports: Inclusive = 4440 to 4440
  ports.map{port =>
    system.actorOf(Props(classOf[SnrksMonitor3], "192.168.1.144", port, name))
  }

  val bindingFuture = Http().bindAndHandle(Routes.route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}


