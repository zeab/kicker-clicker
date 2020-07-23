package com.zeab.kickerclicker

import java.sql.{Connection, DriverManager, ResultSet}

object SQLConnection {

  val user: String = System.getenv("MYSQL_USER")
  val password: String = System.getenv("MYSQL_PASSWORD")
  val url: String = System.getenv("MYSQL_URL")

  val sqlConnection: Connection = DriverManager.getConnection(s"jdbc:mysql://$url:3306",user,password)

  def selectUsers(connection: Connection): List[(String, String, String)] ={
    val rs: ResultSet = connection.createStatement().executeQuery("SELECT email, password, cv FROM kicker.users")
    def worker(resultSet: ResultSet, currentList: List[(String, String, String)] = List.empty): List[(String, String, String)] ={
      if(resultSet.next()) {
        val email: String = rs.getString("email")
        val password: String = rs.getString("password")
        val cv: String = rs.getString("cv")
        worker(resultSet, currentList ++ List((email, password, cv)))
      }
      else currentList
    }
    worker(rs)
  }

}
