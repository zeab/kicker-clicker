package com.zeab.kickerclicker.reebok

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZoneId}
import java.util.{Locale, UUID}

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, OneForOneStrategy, SupervisorStrategy}
import com.zeab.kickerclicker.selenium.Selenium
import com.zeab.kickerclicker.sqlconnection.SQLConnection
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.{By, WebElement}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class ReebokReleaseMonitor extends Actor{

  implicit val ec: ExecutionContext = context.system.dispatcher

  override val supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 1, withinTimeRange = 1.minute) {
      case _ => Stop
    }

  val url = "https://www.reebok.com/us/release-dates"

  def receive: Receive = {
    case Start =>
      Selenium.firefox("192.168.1.144", "4440") match {
        case Failure(exception: Throwable) =>
          context.stop(self)
        case Success(webDriver: RemoteWebDriver) =>
          webDriver.manage().window().maximize()
          webDriver.get(url)
          Try(webDriver.findElements(By.xpath("//div[contains(@class, 'plc-product-card')]"))) match {
            case Failure(exception) =>
              println()
            case Success(value) =>
              val hh = value
              val sss = value.asScala.toList.map{xx =>
                Try(xx.findElement(By.xpath(".//div[contains(@class, 'gl-product-card')]"))) match {
                  case Failure(exception) =>
                    println()
                  case Success(value) =>
                    val jj = value.getTagName
                    val ii = value.getText
                    val url: String = value.getAttribute("href")
                    println()
                }
                val t = xx.getText
                val r = xx.getTagName
                (t, r)
              }
              println()
          }
      }
  }

  override def preStart(): Unit = {
    self ! Start
    context.system.scheduler.scheduleOnce(24.hours)(self ! Start)
  }

  case object Start

  case object GetWebSite

}


