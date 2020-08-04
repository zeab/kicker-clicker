package com.zeab.kickerclicker2.brands.snrks

import java.time.ZonedDateTime

import akka.actor.Actor

class SnrksDrops(id: String, url: String, dropDateTime: ZonedDateTime) extends Actor {

  def receive: Receive = {
    case _ =>
  }

  override def preStart(): Unit = {
    println("nike starting")
  }

}
