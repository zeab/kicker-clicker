package com.zeab.kickerclicker3.app.sqlconnection

import java.sql.ResultSet

import com.zeab.kickerclicker3.app.sqlconnection.MYSQLConnection.mySqlConnection
import com.zeab.kickerclicker3.app.sqlconnection.tables.UserTable

trait Users {

  def selectUsers: List[UserTable] = {
    val rs: ResultSet = mySqlConnection.createStatement().executeQuery("SELECT email, password, cv FROM kicker.users")

    @scala.annotation.tailrec
    def worker(resultSet: ResultSet, currentList: List[UserTable] = List.empty): List[UserTable] =
      if (resultSet.next()) {
        val email: String = rs.getString("email")
        val password: String = rs.getString("password")
        val cv: String = rs.getString("cv")
        worker(resultSet, currentList ++ List(UserTable(email, password, cv)))
      }
      else currentList

    worker(rs)
  }

}

