//package com.zeab.kickerclicker.monitor
//
//import java.time.ZonedDateTime
//
//import akka.actor.{ActorRef, ActorSystem, Props}
//import com.zeab.kickerclicker.snrks.Snkrs
//
////TODO I hate all of this but i cant think of something better to call it
//
//object MonitorFactory {
//
//  def startMonitor(id: String, url: String, dateTime: ZonedDateTime)(implicit system: ActorSystem): ActorRef =
//    url match {
//      case x: String if x.contains("nike.com") => system.actorOf(Props(classOf[Snkrs], id, url, dateTime))
//      //case x: String if x.contains("footlocker.com") => system.actorOf(Props(classOf[Monitor], id, url, dateTime))
//      //case x: String if x.contains("adidas.com") => system.actorOf(Props(classOf[Monitor], id, url, dateTime))
//      //case x: String if x.contains("eastbay.com") => system.actorOf(Props(classOf[Monitor], id, url, dateTime))
//      case _ => system.actorOf(Props(classOf[Monitor], id, url, dateTime.toString))
//    }
//
//}
