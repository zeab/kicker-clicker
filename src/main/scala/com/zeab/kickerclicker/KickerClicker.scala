package com.zeab.kickerclicker

import java.time.ZonedDateTime

import akka.Done
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import com.zeab.kickerclicker.eastbay.EastbayReleaseMonitor
import com.zeab.kickerclicker.httpservice.Routes
import com.zeab.kickerclicker.monitor.MonitorFactory
import com.zeab.kickerclicker.sqlconnection.{SQLConnection, SelectDrops}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Promise}

object KickerClicker extends App {

  //Actor System Stuff
  implicit val system: ActorSystem = ActorSystem("Kicker-Clicker", ConfigFactory.load())
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  //start the easy bay monitor
  system.actorOf(Props[EastbayReleaseMonitor])

  //This is not elegant so I need to make it more so
  //val allValidDrops: List[SelectDrops] =
  //  SQLConnection.selectDrops().filter((drop: SelectDrops) => ZonedDateTime.parse(drop.dateTime).isAfter(ZonedDateTime.now().minusDays(1)))

  //val monitors: List[ActorRef] =
  //  allValidDrops.map((drop: SelectDrops) => MonitorFactory.startMonitor(drop.id, drop.url, ZonedDateTime.parse(drop.dateTime)))

  val f = for { bindingFuture <- Http().bindAndHandle(Routes.route, "0.0.0.0", 7000)
                waitOnFuture  <- Promise[Done].future }
    yield waitOnFuture
  sys.addShutdownHook {
    // cleanup logic
  }
  Await.ready(f, Duration.Inf)

}
