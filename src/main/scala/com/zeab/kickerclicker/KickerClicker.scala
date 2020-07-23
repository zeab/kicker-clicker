package com.zeab.kickerclicker

import java.time.ZonedDateTime

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import com.zeab.kickerclicker.httpservice.Routes

import scala.concurrent.ExecutionContext
import scala.io.StdIn

object KickerClicker extends App {

  println(ZonedDateTime.now())

  //Set up stuff
  System.setProperty("webdriver.gecko.driver", System.getenv("DRIVER_LOCATION"))

  //Actor System Stuff
  implicit val system: ActorSystem = ActorSystem("Kicker-Clicker", ConfigFactory.load())
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  val bindingFuture = Http().bindAndHandle(Routes.route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}


