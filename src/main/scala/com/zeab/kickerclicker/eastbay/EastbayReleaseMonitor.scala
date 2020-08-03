package com.zeab.kickerclicker.eastbay

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

class EastbayReleaseMonitor extends Actor{

  implicit val ec: ExecutionContext = context.system.dispatcher

  override val supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 1, withinTimeRange = 1.minute) {
      case _ => Stop
    }

  val url = "https://www.eastbay.com/release-dates.html"

  def receive: Receive = {
    case Start =>
      Selenium.firefox("192.168.1.144", "4440") match {
        case Failure(exception: Throwable) =>
          context.stop(self)
        case Success(webDriver: RemoteWebDriver) =>
          webDriver.manage().window().maximize()
          webDriver.get(url)
          Try(webDriver.findElement(By.className("c-release-calender-details"))) match {
            case Failure(exception) =>
              val hh = exception
              println(hh.toString)
            case Success(releaseCalendar) =>
              //TODO This is also crazy ugly so i also need to fix it badly
              val releases: List[WebElement] = releaseCalendar.findElements(By.className("c-release-product-link")).asScala.toList
              val releaseInfo = releases.map{element: WebElement =>
                val url: String = element.getAttribute("href")
                val text = element.getText.split('\n')
                val date = text(0)
                val formatter = DateTimeFormatter.ofPattern("MMM d yyyy", Locale.US)
                val localDate = LocalDate.parse(date + " 2020", formatter)
                val actualReleaseDate = localDate.atTime(7, 0).atZone( ZoneId.of("America/Los_Angeles"))
                val name = text(1)
                val color = text(2)
                (name, color , url, actualReleaseDate)
              }
              val knownDrops = SQLConnection.selectDrops()
              //TODO This is crazy ugly so ... fix it ...
              val hrh =
              releaseInfo.map{releaseInfo =>
                val ff =
                knownDrops.filter(_.url == releaseInfo._3)
                if (ff.isEmpty) {
                  val id: String = UUID.randomUUID().toString
                  SQLConnection.insertDrop(id, releaseInfo._1, releaseInfo._2, releaseInfo._3, releaseInfo._4.toString, "1", "0")
                }
                else println("drop is already found so skipping insert")
              }
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


