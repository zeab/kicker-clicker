package com.zeab.kickerclicker.snrks2

import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import java.util.{Date, Timer, TimerTask}

import akka.actor.Actor

import scala.util.{Failure, Success, Try}

class SnrksMonitor2(name: String, size: String, isMale: Boolean, email: String, password: String, cv: String, startDateTime: String) extends Actor{

  def receive: Receive = {
    case Start =>
        println(s"im starting ${self.path.name}")
      //I need to start doing stuff
  }

  override def preStart(): Unit = {
    Try(ZonedDateTime.parse(startDateTime)) match {
      case Failure(exception: Throwable) => println(exception)
      case Success(actualStartDateTime: ZonedDateTime) =>
        val timer: Timer = new Timer()
        val task: TimerTask = new TimerTask() { override def run(): Unit = { self ! Start } }
        timer.schedule(task, Date.from(actualStartDateTime.minusMinutes(5).toInstant), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS))
    }
  }

  case object Start

}


//
//actually i think i need to re think how i deploy the browers like how do i do it on mass for a
//
//
//
//shoe_name: String
//shoe_sizes: List[String]
//drop_date_time: ZonedDateTime
//users: List[String]
//
//
//what kinda respources so i actaully need...






