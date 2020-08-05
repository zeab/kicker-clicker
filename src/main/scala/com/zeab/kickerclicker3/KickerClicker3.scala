package com.zeab.kickerclicker3

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import com.zeab.kickerclicker3.app.appconf.AppConf
import com.zeab.kickerclicker3.app.sqlconnection.MYSQLConnection
import com.zeab.kickerclicker3.businesslogic.Routes
import com.zeab.kickerclicker3.businesslogic.footlocker.FootLockerReleaseDateMonitor
import com.zeab.kickerclicker3.businesslogic.snrks.SnrksReleaseDateMonitor

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.sys.addShutdownHook
import scala.concurrent.duration._

object KickerClicker3 extends App {

  //Actor System Stuff
  implicit val system: ActorSystem = ActorSystem("Kicker-Clicker", ConfigFactory.load())
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  //Start release date monitors
  val snrksReleaseDateMonitor: ActorRef =
    system.actorOf(Props(classOf[SnrksReleaseDateMonitor]), "snrks-release-date-monitor")
  val footLockerReleaseDateMonitor: ActorRef =
    system.actorOf(Props(classOf[FootLockerReleaseDateMonitor]), "foot-locker-release-date-monitor")

  //Start drop monitors
  MYSQLConnection.selectDrops().foreach{ drop =>
    //start a drop monitor which will deal with the filtering of stuff
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
