package com.zeab.kickerclicker3.app.appconf

object AppConf {

  //TODO Move these to the app conf for real

  //Http Service
  val httpHost: String = "0.0.0.0"
  val httpPort: Int = 7000

  //MySQL
  val mySqlUser: String = System.getenv("MYSQL_USER")
  val mySqlPassword: String = System.getenv("MYSQL_PASSWORD")
  val mySqlHost: String = System.getenv("MYSQL_HOST")
  val mySqlPort: String = System.getenv("MYSQL_PORT") //3306

  //Selenium
  val seleniumScreenShotDir: String = System.getenv("SELENIUM_SCREENSHOT_DIR")
  val seleniumWebDriverLoc: String = System.getenv("SELENIUM_WEB_DRIVER_LOC")
  val seleniumRemoteDriverHost: String = System.getenv("SELENIUM_REMOTE_DRIVER_HOST")
  val seleniumRemoteDriverPort: String = System.getenv("SELENIUM_REMOTE_DRIVER_PORT")

}
