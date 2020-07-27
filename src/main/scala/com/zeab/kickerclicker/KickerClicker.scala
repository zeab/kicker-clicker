package com.zeab.kickerclicker

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import com.zeab.kickerclicker.httpservice.Routes

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Promise}

object KickerClicker extends App {

  //Actor System Stuff
  implicit val system: ActorSystem = ActorSystem("Kicker-Clicker", ConfigFactory.load())
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  val f = for { bindingFuture <- Http().bindAndHandle(Routes.route, "0.0.0.0", 7000)
                waitOnFuture  <- Promise[Done].future }
    yield waitOnFuture
  sys.addShutdownHook {
    // cleanup logic
  }
  Await.ready(f, Duration.Inf)

}
