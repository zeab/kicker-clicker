package com.zeab.kickerclicker2.brands.footlocker

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZoneId, ZonedDateTime}
import java.util
import java.util.{Locale, UUID}

import akka.actor.Actor
import com.zeab.kickerclicker2.selenium.Selenium
import com.zeab.kickerclicker2.sqlconnection.SQLConnection
import com.zeab.kickerclicker2.sqlconnection.tables.DropsTable
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.{By, WebElement}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class FootLockerReleaseDates(subBrand: String) extends Actor {

  implicit val ec: ExecutionContext = context.system.dispatcher

  val url: String =
    subBrand match{
      case "footlocker" => "https://www.footlocker.com/release-dates"
      case "eastbay" => "https://www.eastbay.com/release-dates.html"
    }

  def receive: Receive = openDriver

  def openDriver: Receive = {
    case OpenDriver =>
      Selenium.firefox("192.168.1.144", "4440") match {
        case Failure(exception: Throwable) =>
          println(exception)
          context.system.stop(self)
        case Success(webDriver: RemoteWebDriver) =>
          context.become(openUrl(webDriver))
          self ! OpenUrl
      }
  }

  def openUrl(webDriver: RemoteWebDriver): Receive = {
    case OpenUrl =>
      webDriver.manage().window().maximize()
      webDriver.get(url)
      context.become(checkReleases(webDriver))
      self ! CheckReleases
  }

  def checkReleases(webDriver: RemoteWebDriver): Receive = {
    case CheckReleases =>
      Try(webDriver.findElements(By.className("c-release-product-link"))) match {
        case Failure(exception: Throwable) =>
          println(exception)
          //TODO Need to figure out the areas in which this might fail
          webDriver.quit()
          context.system.stop(self)
        case Success(productCards: util.List[WebElement]) =>

          val foundDrops: List[DropsTable] =
            productCards.asScala.toList
              .filter { productCard: WebElement =>
                !productCard.getText.split("\n").isEmpty
              }
              .map { productCard: WebElement =>
                val url: String = productCard.getAttribute("href")
                val text: Array[String] = productCard.getText.split('\n')
                val date: String = text(0)
                val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d yyyy", Locale.US)
                val localDate: LocalDate = LocalDate.parse(date + " 2020", formatter)
                val actualReleaseDate: ZonedDateTime = localDate.atTime(7, 0).atZone( ZoneId.of("America/Los_Angeles"))
                val name: String = text(1)
                val color: String = text(2)
                DropsTable("", name, color, url, actualReleaseDate.toString, "1", "0")
              }

          val knownDrops: List[DropsTable] = SQLConnection.selectDrops()

          foundDrops.foreach { foundDrop: DropsTable =>
            if (knownDrops.exists(_.url == foundDrop.url))
              println("drop is already found so skipping insert")
            else {
              val id: String = UUID.randomUUID().toString
              SQLConnection.insertDrop(id, foundDrop.name, foundDrop.color, foundDrop.url, foundDrop.dateTime, "1", "0")
            }
          }

          webDriver.quit()
      }
  }

  override def preStart(): Unit = {
    self ! OpenDriver
    context.system.scheduler.scheduleOnce(4.hours)(self ! OpenDriver)
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    println("stopping monitor")
    context.system.stop(self)
  }

  case object OpenDriver

  case object OpenUrl

  case object CheckReleases

}
