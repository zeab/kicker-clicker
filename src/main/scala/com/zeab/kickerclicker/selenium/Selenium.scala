package com.zeab.kickerclicker.selenium

import java.io.File
import java.net.URL

import org.apache.commons.io.FileUtils
import org.openqa.selenium.{OutputType, TakesScreenshot, WebDriver}
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.remote.RemoteWebDriver

import scala.util.Try

object Selenium {

  def firefox(host: String, port: String): Try[RemoteWebDriver] ={
    val options: FirefoxOptions = new FirefoxOptions
    //Set for headless mode
    options.setHeadless(true)
    //Set so we don't load images
    options.addPreference("permissions.default.image", 2)
    options.addPreference("dom.ipc.plugins.enabled.libflashplayer.so", "false")
    //Set the zoom so we see the entire page
    options.addPreference("layout.css.devPixelsPerPx", "0.5")
    //options.addPreference("network.proxy.http", "localhost")
    //options.addPreference("network.proxy.http_port", 7000)
    //options.addPreference("network.proxy.type", 1)
    Try(new RemoteWebDriver(new URL(s"http://$host:$port/wd/hub"), options))
  }

  def takeScreenshot(id: String)(implicit webDriver: WebDriver): Unit = {
    val file: File = webDriver.asInstanceOf[TakesScreenshot].getScreenshotAs(OutputType.FILE)
    FileUtils.copyFile(file, new File(s"/selenium/$id/${System.currentTimeMillis()}.png"))
  }

}
