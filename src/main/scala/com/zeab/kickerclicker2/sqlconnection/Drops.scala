//package com.zeab.kickerclicker2.sqlconnection
//
//import java.sql.ResultSet
//
//import com.zeab.kickerclicker2.sqlconnection.SQLConnection.sqlConnection
//import com.zeab.kickerclicker2.sqlconnection.tables.DropsTable
//
//trait Drops {
//
//  def selectDrops(id: Option[String] = None, url: Option[String] = None): List[DropsTable] = {
//    val query: String =
//      (id, url) match {
//        case (Some(foundId: String), _) =>
//          s"SELECT * FROM kicker.drops WHERE id = '$foundId'"
//        case (_, Some(foundUrl: String)) =>
//          s"SELECT * FROM kicker.drops WHERE url = '$foundUrl'"
//        case (None, None) =>
//          "SELECT * FROM kicker.drops"
//      }
//    val rs: ResultSet = sqlConnection.createStatement().executeQuery(query)
//
//    @scala.annotation.tailrec
//    def worker(resultSet: ResultSet, currentList: List[DropsTable] = List.empty): List[DropsTable] =
//      if (resultSet.next()) {
//        val id: String = rs.getString("id")
//        val url: String = rs.getString("url")
//        val name: String = rs.getString("name")
//        val color: String = rs.getString("color")
//        val dateTime: String = rs.getString("dateTime")
//        val wanted: String = rs.getString("wanted")
//        val monitorPeriod: String = rs.getString("monitorPeriod")
//        worker(resultSet, currentList ++ List(DropsTable(id, name, color, url, dateTime, wanted, monitorPeriod)))
//      }
//      else currentList
//
//    worker(rs)
//  }
//
//  def insertDrop(id: String, name: String, color: String, url: String, dateTime: String, wanted: String, monitorPeriod: String): Unit =
//    sqlConnection.createStatement().executeUpdate(s"INSERT INTO kicker.drops (id, name, color, url, dateTime, wanted, monitorPeriod) VALUES ('$id', '${name.replace("'", "")}', '$color', '$url', '$dateTime', '$wanted', '$monitorPeriod');") match {
//      case 1 => println("the sql drop insert was successful")
//      case _ => println("the drop sql insert does not seem to have been successful")
//    }
//
//  def deleteDrop(id: String): Unit =
//    sqlConnection.createStatement().executeUpdate(s"DELETE FROM kicker.drops WHERE id = '$id';") match {
//      case 1 => println("the sql drop delete was successful")
//      case _ => println("the drop sql delete does not seem to have been successful")
//    }
//
//}
