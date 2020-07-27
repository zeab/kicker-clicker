package com.zeab.kickerclicker.sqlconnection

import java.sql.{Connection, DriverManager, ResultSet}

object SQLConnection {

  val user: String = System.getenv("MYSQL_USER")
  val password: String = System.getenv("MYSQL_PASSWORD")
  val url: String = System.getenv("MYSQL_URL")

  val sqlConnection: Connection = DriverManager.getConnection(s"jdbc:mysql://$url:3306",user,password)

  def selectUsers: List[(String, String, String)] ={
    val rs: ResultSet = sqlConnection.createStatement().executeQuery("SELECT email, password, cv FROM kicker.users")
    @scala.annotation.tailrec
    def worker(resultSet: ResultSet, currentList: List[(String, String, String)] = List.empty): List[(String, String, String)] =
      if(resultSet.next()) {
        val email: String = rs.getString("email")
        val password: String = rs.getString("password")
        val cv: String = rs.getString("cv")
        worker(resultSet, currentList ++ List((email, password, cv)))
      }
      else currentList
    worker(rs)
  }

  def selectDrops(id: Option[String] = None): List[SelectDrops] ={
    val query: String =
      id match {
        case Some(foundId: String) =>
          s"SELECT id, url, dateTime, monitorPeriod FROM kicker.drops WHERE id = '$foundId'"
        case None =>
          "SELECT id, url, dateTime, monitorPeriod FROM kicker.drops"
      }
    val rs: ResultSet = sqlConnection.createStatement().executeQuery(query)
    @scala.annotation.tailrec
    def worker(resultSet: ResultSet, currentList: List[SelectDrops] = List.empty): List[SelectDrops] =
      if(resultSet.next()) {
        val id: String = rs.getString("id")
        val url: String = rs.getString("url")
        val dateTime: String = rs.getString("dateTime")
        val monitorPeriod: String = rs.getString("monitorPeriod")
        worker(resultSet, currentList ++ List(SelectDrops(id, url, dateTime, monitorPeriod)))
      }
      else currentList
    worker(rs)
  }

  def insertDrop(id: String, url: String, dateTime: String, monitorPeriod: String): Unit =
    sqlConnection.createStatement().executeUpdate(s"INSERT INTO kicker.drops (id, url, dateTime, monitorPeriod) VALUES ('$id', '$url', '$dateTime', '$monitorPeriod');") match {
      case 1 => println("the sql drop insert was successful")
      case _ => println("the drop sql insert does not seem to have been successful")
    }

  def deleteDrop(id: String): Unit =
    sqlConnection.createStatement().executeUpdate(s"DELETE FROM kicker.drops WHERE id = '$id';") match {
      case 1 => println("the sql drop delete was successful")
      case _ => println("the drop sql delete does not seem to have been successful")
    }

}
