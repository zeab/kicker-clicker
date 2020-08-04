package com.zeab.kickerclicker2.sqlconnection

import java.sql.ResultSet

import com.zeab.kickerclicker2.sqlconnection.SQLConnection.sqlConnection

trait Users {

  def selectUsers: List[(String, String, String)] = {
    val rs: ResultSet = sqlConnection.createStatement().executeQuery("SELECT email, password, cv FROM kicker.users")

    @scala.annotation.tailrec
    def worker(resultSet: ResultSet, currentList: List[(String, String, String)] = List.empty): List[(String, String, String)] =
      if (resultSet.next()) {
        val email: String = rs.getString("email")
        val password: String = rs.getString("password")
        val cv: String = rs.getString("cv")
        worker(resultSet, currentList ++ List((email, password, cv)))
      }
      else currentList

    worker(rs)
  }

}

