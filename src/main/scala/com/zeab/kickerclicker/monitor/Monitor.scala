package com.zeab.kickerclicker.monitor

import java.io.File
import java.net.URL
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import java.util.{Date, Timer, TimerTask}

import akka.actor.Actor
import org.apache.commons.io.FileUtils
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.{OutputType, TakesScreenshot, WebDriver}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class Monitor(id: String, url: String, dateTime: String) extends Actor {

  val x = new URL(url)

  val host = "192.168.1.144"
  val port = 4440

  implicit val ec: ExecutionContext = context.system.dispatcher

  def receive: Receive = inactive

  def inactive: Receive = {
    case Init =>
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
      val firefox: WebDriver = new RemoteWebDriver(new URL(s"http://$host:$port/wd/hub"), options)
      context.become(active(0)(firefox))
      self ! OpenWebSite
  }

  def active(count: Int = 0)(implicit firefox: WebDriver): Receive = {
    case OpenWebSite =>
      firefox.manage().window().maximize()
      println(s"opening website $url")
      firefox.get(url)
      firefox.getTitle match {
        case null =>
          takeScreenshot(id, x.getPath.replace('/', '-'), "error")
          println("the web title is null so i don't think we hit what we expected")
          //throw new Exception("the web title is null so i don't think we hit what we expected")
          firefox.close()
          context.system.stop(self)
        case _ =>
          println("the site seems to be up so moving on to the next step")
          takeScreenshot(id, x.getPath.replace('/', '-'))
          context.system.scheduler.scheduleOnce(30.second) (self ! Refresh)
      }
    case Refresh =>
      if (count == 10) {
        println("closing up shop")
        firefox.close()
        context.system.stop(self)
      }
      else {
        println("prep refresh")
        context.become(active(count + 1))
        takeScreenshot(id, x.getPath.replace('/', '-'))
        context.system.scheduler.scheduleOnce(30.second) (self ! Refresh)
        println("refreshing")
        firefox.navigate().refresh()
      }
  }

  override def preStart(): Unit = {
    println(s"setting monitor for $dateTime")
    Try(ZonedDateTime.parse(dateTime)) match {
      case Failure(exception: Throwable) => println(exception)
      case Success(actualStartDateTime: ZonedDateTime) =>
        val timer: Timer = new Timer()
        val task: TimerTask = new TimerTask() { override def run(): Unit = { self ! Init } }
        timer.schedule(task, Date.from(actualStartDateTime.minusMinutes(5).toInstant), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS))
    }
  }

  def takeScreenshot(id: String, name: String, suffix: String = "live")(implicit webDriver: WebDriver): Unit = {
    val file: File = webDriver.asInstanceOf[TakesScreenshot].getScreenshotAs(OutputType.FILE)
    FileUtils.copyFile(file, new File(s"/selenium/$id/${System.currentTimeMillis()}$name-$suffix.png"))
  }

  case object Init

  case object OpenWebSite

  case object Refresh

}
