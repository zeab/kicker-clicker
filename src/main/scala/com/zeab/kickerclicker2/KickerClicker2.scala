//package com.zeab.kickerclicker2
//
//import java.net.URL
//import java.time.ZonedDateTime
//
//import akka.actor.{ActorSystem, Props}
//import akka.http.scaladsl.Http
//import akka.stream.ActorMaterializer
//import com.typesafe.config.ConfigFactory
//import com.zeab.kickerclicker2.appconf.AppConf
//import com.zeab.kickerclicker2.brands.adidas.AdidasReleaseDates
//import com.zeab.kickerclicker2.brands.footlocker.{FootLockerDrops, FootLockerReleaseDates}
//import com.zeab.kickerclicker2.brands.reebok.ReebokReleaseDates
//import com.zeab.kickerclicker2.brands.snrks.{SnrksDrops, SnrksReleaseDates}
//import com.zeab.kickerclicker2.httpservice.Routes
//import com.zeab.kickerclicker2.sqlconnection.SQLConnection
//
//import scala.concurrent.duration._
//import scala.concurrent.{Await, ExecutionContext, Future}
//import scala.sys.addShutdownHook
//import scala.util.{Failure, Success, Try}
//
//object KickerClicker2 extends App {
//
//  val isLocal: Boolean = false
//
//  //Actor System Stuff
//  implicit val system: ActorSystem = ActorSystem("Kicker-Clicker", ConfigFactory.load())
//  implicit val mat: ActorMaterializer = ActorMaterializer()
//  implicit val ec: ExecutionContext = system.dispatcher
//
//  //Start Release Monitors
//  if (isLocal){
//    //system.actorOf(Props[AdidasReleaseDates])
//    //system.actorOf(Props(classOf[FootLockerReleaseDates], "footlocker"))
//    //system.actorOf(Props(classOf[FootLockerReleaseDates], "eastbay"))
//    //system.actorOf(Props[ReebokReleaseDates])
//    system.actorOf(Props(classOf[SnrksReleaseDates], None, None))
//  }
//  else{
//    //system.actorOf(Props[AdidasReleaseDates])
//    //system.actorOf(Props(classOf[FootLockerReleaseDates], "footlocker"))
//    //system.actorOf(Props(classOf[FootLockerReleaseDates], "eastbay"))
//    //system.actorOf(Props[ReebokReleaseDates])
//    system.actorOf(Props(classOf[SnrksReleaseDates], Some("192.168.1.144"), Some("4440")))
//  }
//
//  //Read into the database and start Drop monitors
////  SQLConnection.selectDrops().map{ drop =>
////    Try(new URL(drop.url)) match {
////      case Failure(exception) =>
////        println(exception.toString)
////      case Success(url) =>
////        url.getHost match {
////          case "www.nike.com" =>
////            system.actorOf(Props(classOf[SnrksDrops], drop.id, drop.url, ZonedDateTime.parse(drop.dateTime)))
////          case "www.eastbay.com" =>
////            system.actorOf(Props(classOf[FootLockerDrops], drop.id, drop.url, ZonedDateTime.parse(drop.dateTime)))
////          case _ => println("no actor found")
////        }
////        println()
////    }
////  }
//
//  //Bind the routes to the port
//  val httpService: Future[Http.ServerBinding] =
//    Http().bindAndHandle(Routes.businessLogic, AppConf.httpHost, AppConf.httpPort)
//
//  //Shutdown
//  addShutdownHook {
//    httpService.onComplete { _ =>
//      system.terminate()
//      Await.result(system.whenTerminated, 30.seconds)
//    }
//  }
//
//}
