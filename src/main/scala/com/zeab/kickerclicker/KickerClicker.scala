package com.zeab.kickerclicker

import java.util.concurrent.TimeUnit
import java.util.{Calendar, Timer, TimerTask}

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import com.zeab.kickerclicker.snrks.SnrksMonitor

import scala.concurrent.ExecutionContext

object KickerClicker extends App {

  //Set up stuff
  System.setProperty("webdriver.gecko.driver", System.getenv("DRIVER_LOCATION"))

  //Actor System Stuff
  implicit val system: ActorSystem = ActorSystem("Shoes", ConfigFactory.load())
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  val name: String = System.getenv("SHOE_NAME")
  val size: String = System.getenv("SHOE_SIZE")
  val isMale: Boolean = System.getenv("IS_MALE").contains("true")

  system.actorOf(Props(classOf[SnrksMonitor], name, size, isMale, false))

  //what about when do we actually start the bot going?
  val today: Calendar = Calendar.getInstance()
  today.set(Calendar.HOUR_OF_DAY, 6)
  today.set(Calendar.MINUTE, 58)
  today.set(Calendar.SECOND, 0)
  val timer: Timer = new Timer()
  val task: TimerTask = new TimerTask() {
    override def run(): Unit = {
      //system.actorOf(Props(classOf[SnrksMonitor], name, size, isMale, false))
    }
  }
  timer.schedule(task, today.getTime, TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS))

}
