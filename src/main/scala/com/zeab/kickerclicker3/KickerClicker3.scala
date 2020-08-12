package com.zeab.kickerclicker3

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import com.zeab.kickerclicker3.app.appconf.AppConf
import com.zeab.kickerclicker3.app.sqlconnection.MYSQLConnection
import com.zeab.kickerclicker3.businesslogic.adidas.AdidasReleaseDateMonitor
import com.zeab.kickerclicker3.businesslogic.eastbay.{EastbayDropMonitor, EastbayReleaseDateMonitor}
import com.zeab.kickerclicker3.businesslogic.http.Routes
import com.zeab.kickerclicker3.businesslogic.snrks.{SnrksDropMonitor, SnrksReleaseDateMonitor}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.sys.addShutdownHook

object KickerClicker3 extends App {

  //Actor System Stuff
  implicit val system: ActorSystem = ActorSystem("Kicker-Clicker", ConfigFactory.load())
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  //Start release date monitors
  val snrksReleaseDateMonitor: ActorRef =
    system.actorOf(Props(classOf[SnrksReleaseDateMonitor]), "snrks-release-date-monitor")
//  val eastbayReleaseDateMonitor: ActorRef =
//    system.actorOf(Props(classOf[EastbayReleaseDateMonitor]), "eastbay-release-date-monitor")
//  val adidasReleaseDateMonitor: ActorRef =
//    system.actorOf(Props(classOf[AdidasReleaseDateMonitor]), "adidas-release-date-monitor")
  //  val bodegaReleaseDateMonitor: ActorRef =
  //    system.actorOf(Props(classOf[BodegaReleaseDateMonitor]), "bodega-release-date-monitor")

  //Start up all the drop monitors based off known upcoming drops
  MYSQLConnection.selectDrops().foreach {
    case drop if drop.url.contains("www.nike.com") =>
      system.actorOf(Props(classOf[SnrksDropMonitor], drop.id, drop.url, drop.dateTime))
    case drop if drop.url.contains("www.eastbay.com") =>
      system.actorOf(Props(classOf[EastbayDropMonitor], drop.id, drop.url, drop.dateTime))
    case drop => println(s"${drop.url} is not supported yet")
  }

  //Bind the routes to the port
  val httpService: Future[Http.ServerBinding] =
    Http().bindAndHandle(Routes.routes, AppConf.httpHost, AppConf.httpPort)

  //Shutdown
  addShutdownHook {
    httpService.onComplete { _ =>
      system.terminate()
      Await.result(system.whenTerminated, 30.seconds)
    }
  }

}
