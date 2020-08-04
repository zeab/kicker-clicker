package com.zeab.kickerclicker2.sqlconnection

import java.sql.{Connection, DriverManager}

object SQLConnection extends Drops with Users {
  val user: String = System.getenv("MYSQL_USER")
  val password: String = System.getenv("MYSQL_PASSWORD")
  val url: String = System.getenv("MYSQL_URL")

  val sqlConnection: Connection = DriverManager.getConnection(s"jdbc:mysql://$url:3306", user, password)
}
