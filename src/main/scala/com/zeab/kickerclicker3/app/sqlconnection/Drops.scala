package com.zeab.kickerclicker3.app.sqlconnection

import java.sql.ResultSet

import com.zeab.kickerclicker3.app.sqlconnection.MYSQLConnection.mySqlConnection
import com.zeab.kickerclicker3.app.sqlconnection.tables.DropsTable

trait Drops {

  def selectDrops(id: Option[String] = None, url: Option[String] = None): List[DropsTable] = {
    val query: String =
      (id, url) match {
        case (Some(foundId: String), _) =>
          s"SELECT * FROM kicker.drops WHERE id = '$foundId'"
        case (_, Some(foundUrl: String)) =>
          s"SELECT * FROM kicker.drops WHERE url = '$foundUrl'"
        case (None, None) =>
          "SELECT * FROM kicker.drops"
      }
    val rs: ResultSet = mySqlConnection.createStatement().executeQuery(query)

    @scala.annotation.tailrec
    def worker(resultSet: ResultSet, currentList: List[DropsTable] = List.empty): List[DropsTable] =
      if (resultSet.next()) {
        val id: String = rs.getString("id")
        val url: String = rs.getString("url")
        val imageUrl: String = rs.getString("image_url")
        val name: String = rs.getString("name")
        val color: String = rs.getString("color")
        val dateTime: Long = rs.getLong("date_time")
        val isWanted: Boolean = rs.getBoolean("is_wanted")
        worker(resultSet, currentList ++ List(DropsTable(id, name, color, url, imageUrl, dateTime, isWanted)))
      }
      else currentList

    worker(rs)
  }

  def insertDrop(id: String, name: String, color: String, url: String, imageUrl: String, dateTime: Long, isWanted: Boolean): Unit = {
    val removeBadCharsFromName: String = name.replace("'", "")
    mySqlConnection.createStatement().executeUpdate(s"INSERT INTO kicker.drops (id, name, color, url, image_url, date_time, is_wanted) VALUES ('$id', '$removeBadCharsFromName', '$color', '$url', '$imageUrl', $dateTime, $isWanted);") match {
      case 1 => println("the sql drop insert was successful")
      case _ => println("the drop sql insert does not seem to have been successful")
    }
  }

  def deleteDrop(id: String): Unit =
    mySqlConnection.createStatement().executeUpdate(s"DELETE FROM kicker.drops WHERE id = '$id';") match {
      case 1 => println("the sql drop delete was successful")
      case _ => println("the drop sql delete does not seem to have been successful")
    }

}
