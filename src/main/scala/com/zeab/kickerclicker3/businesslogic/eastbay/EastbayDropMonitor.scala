package com.zeab.kickerclicker3.businesslogic.eastbay

import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import java.util.{Date, Timer, TimerTask}

import akka.actor.{Actor, Props}
import com.zeab.kickerclicker3.app.sqlconnection.MYSQLConnection
import com.zeab.kickerclicker3.app.sqlconnection.tables.UserTable

class EastbayDropMonitor(id: String, url: String, dateTime: String) extends Actor{

  def receive: Receive = {
    case SpawnBuyers =>
      println(s"eastbay spawning users for $url")
      MYSQLConnection.selectUsers.foreach{ user: UserTable =>
        println(s"eastbay buyer for ${user.email}")
        context.actorOf(Props(classOf[EastbayBuyer], id, url, user.email, user.password, user.cv))
      }
  }

  override def preStart(): Unit = {
    val now: ZonedDateTime = ZonedDateTime.now()
    val dropDateTime: ZonedDateTime = ZonedDateTime.parse(dateTime)
    if (now.isBefore(dropDateTime)) {
      val timer: Timer = new Timer()
      val task: TimerTask = new TimerTask() { override def run(): Unit = { self ! SpawnBuyers } }
      println(s"setting $url to start buying at $dropDateTime")
      timer.schedule(task, Date.from(dropDateTime.toInstant), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS))
    }
    else {
      println(s"$url has passed the drop date time stopping drop monitor")
      context.stop(self)
    }
  }

  case object SpawnBuyers

}