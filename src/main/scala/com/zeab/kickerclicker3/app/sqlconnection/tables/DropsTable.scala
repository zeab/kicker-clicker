package com.zeab.kickerclicker3.app.sqlconnection.tables

case class DropsTable(
                       id: String,
                       name: String,
                       color: String,
                       url: String,
                       dateTime: Long,
                       isWanted: Boolean
                     )
