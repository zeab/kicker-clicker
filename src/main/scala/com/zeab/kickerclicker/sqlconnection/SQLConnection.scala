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

  def selectDrops(id: Option[String] = None, url: Option[String] = None): List[SelectDrops] ={
    val query: String =
      (id, url) match {
        case (Some(foundId: String), _) =>
          s"SELECT * FROM kicker.drops WHERE id = '$foundId'"
        case (_, Some(foundUrl: String)) =>
          s"SELECT * FROM kicker.drops WHERE url = '$foundUrl'"
        case (None, None) =>
          "SELECT * FROM kicker.drops"
      }
    val rs: ResultSet = sqlConnection.createStatement().executeQuery(query)
    @scala.annotation.tailrec
    def worker(resultSet: ResultSet, currentList: List[SelectDrops] = List.empty): List[SelectDrops] =
      if(resultSet.next()) {
        val id: String = rs.getString("id")
        val url: String = rs.getString("url")
        val name: String = rs.getString("name")
        val color: String = rs.getString("color")
        val dateTime: String = rs.getString("dateTime")
        val wanted: String = rs.getString("wanted")
        val monitorPeriod: String = rs.getString("monitorPeriod")
        worker(resultSet, currentList ++ List(SelectDrops(id, name, color, url, dateTime, wanted, monitorPeriod)))
      }
      else currentList
    worker(rs)
  }

  def insertDrop(id: String, name: String, color: String, url: String, dateTime: String, wanted: String, monitorPeriod: String): Unit = {
    val dd = s"INSERT INTO kicker.drops (id, name, color, url, dateTime, wanted, monitorPeriod) VALUES ('$id', '${name.replace("'", "")}', '$color', '$url', '$dateTime', '$wanted', '$monitorPeriod');"
    sqlConnection.createStatement().executeUpdate(dd) match {
      case 1 => println("the sql drop insert was successful")
      case _ => println("the drop sql insert does not seem to have been successful")
    }
  }

  def deleteDrop(id: String): Unit =
    sqlConnection.createStatement().executeUpdate(s"DELETE FROM kicker.drops WHERE id = '$id';") match {
      case 1 => println("the sql drop delete was successful")
      case _ => println("the drop sql delete does not seem to have been successful")
    }

}
