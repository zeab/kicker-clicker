package com.zeab.kickerclicker3.app.sqlconnection

import java.sql.{Connection, DriverManager}

import com.zeab.kickerclicker3.app.appconf.AppConf

object MYSQLConnection extends Drops with Users {

  val mySqlConnection: Connection = DriverManager.getConnection(s"jdbc:mysql://${AppConf.mySqlHost}:${AppConf.mySqlPort}", AppConf.mySqlUser, AppConf.mySqlPassword)

}
