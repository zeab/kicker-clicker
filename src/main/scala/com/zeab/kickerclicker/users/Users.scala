package com.zeab.kickerclicker.users

import akka.actor.Actor

class Users extends Actor{

  def receive: Receive = {
    case _ =>
  }

}

object Users extends Users{
  val users: List[(String, String, String)] = List.empty
}


//so nowwwww that i need users how do i actually deal with them?...
//if i user is starting to login i should not give that to any other user
//if i dont have any more users to use then i should close up shop completely

//1 user == 1 shoe
//x shoes x day

