package com.zeab.kickerclicker.users

import akka.actor.Actor

class Users extends Actor{

  def receive: Receive = {
    case _ =>
  }

}

object Users extends Users{
  val users: List[(String, String, String)] = ???
}
