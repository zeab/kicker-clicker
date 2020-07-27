package com.zeab.kickerclicker.snrks

case class PutUser(
                     email: Option[String],
                     password: Option[String],
                     cv: Option[Int]
                   )
