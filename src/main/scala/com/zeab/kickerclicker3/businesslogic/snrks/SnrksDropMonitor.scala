package com.zeab.kickerclicker3.businesslogic.snrks

import java.time.{ZoneId, ZonedDateTime}
import java.util.concurrent.TimeUnit
import java.util.{Date, Timer, TimerTask}

import akka.actor.{Actor, Props}
import com.zeab.kickerclicker3.app.sqlconnection.MYSQLConnection
import com.zeab.kickerclicker3.app.sqlconnection.tables.UserTable

class SnrksDropMonitor(id: String, url: String, dateTime: Long) extends Actor{

  def receive: Receive = {
    case SpawnBuyers =>
      println(s"spawning users for $url")
      MYSQLConnection.selectUsers.foreach{ user: UserTable =>
        println(s"buyer for ${user.email}")
        context.actorOf(Props(classOf[SnrksBuyer], id, url, user.email, user.password, user.cv))
      }
  }

  override def preStart(): Unit = {
    val now: Long = ZonedDateTime.now().toInstant.toEpochMilli
    val dropDateTime: Date = new Date(dateTime)
    if (now < dateTime) {
      val timer: Timer = new Timer()
      val task: TimerTask = new TimerTask() { override def run(): Unit = { self ! SpawnBuyers } }
      println(s"setting $url to start buying at $dropDateTime")
      timer.schedule(task, dropDateTime, TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS))
    }
    else {
      println(s"$url has passed the drop date time stopping drop monitor")
      context.stop(self)
    }
  }

  case object SpawnBuyers

  override def postRestart(reason: Throwable): Unit = {
    println("an exception happened so lets just stop and figure out why ok :)")
    context.stop(self)
  }

}
