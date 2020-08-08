package com.zeab.kickerclicker3.businesslogic.eastbay

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZoneId, ZonedDateTime}
import java.util
import java.util.{Locale, UUID}

import akka.actor.{Actor, Props}
import com.zeab.kickerclicker3.app.appconf.AppConf
import com.zeab.kickerclicker3.app.selenium.{ConnectToWebDriver, GetUrl, Selenium}
import com.zeab.kickerclicker3.app.sqlconnection.MYSQLConnection
import com.zeab.kickerclicker3.app.sqlconnection.tables.DropsTable
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.{By, WebElement}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success, Try}

class EastbayReleaseDateMonitor extends Actor {

  implicit val ec: ExecutionContext = context.system.dispatcher

  val url: String = "https://www.eastbay.com/release-dates.html"

  def receive: Receive = connectToWebDriver

  def connectToWebDriver: Receive = {
    case ConnectToWebDriver =>
      Selenium.firefox(AppConf.seleniumRemoteDriverHost, AppConf.seleniumRemoteDriverPort) match {
        case Failure(exception: Throwable) =>
          println(exception.toString)
          context.system.stop(self)
        case Success(webDriver: RemoteWebDriver) =>
          println("eastbay release monitor remote driver connected")
          context.become(openReleaseCalendar(webDriver))
          self ! GetUrl
      }
  }

  def openReleaseCalendar(webDriver: RemoteWebDriver): Receive = {
    case GetUrl =>
      println("eastbay release monitor getting releases")
      webDriver.manage().window().maximize()
      webDriver.get(url)
      context.become(recordProductCards(webDriver))
      self ! RecordProductCards
  }

  def recordProductCards(webDriver: RemoteWebDriver): Receive = {
    case RecordProductCards =>
      Try(webDriver.findElements(By.className("c-release-product-link"))) match {
        case Failure(exception: Throwable) =>
          println(exception.toString)
          webDriver.quit()
          context.system.stop(self)
        case Success(productCards: util.List[WebElement]) =>
          val foundDrops: List[DropsTable] =
            productCards.asScala.toList
              .filterNot(_.getText == "")
              .map { productCard: WebElement =>
                val url: String = productCard.getAttribute("href")
                val text: Array[String] = productCard.getText.split('\n')
                val date: String = text(0)
                val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d yyyy", Locale.US)
                val localDate: LocalDate = LocalDate.parse(date + " 2020", formatter)
                val actualReleaseDate: ZonedDateTime = localDate.atTime(7, 0).atZone( ZoneId.of("America/Los_Angeles"))
                val name: String = text(1)
                val color: String = text(2)
                DropsTable("", name, color, url, actualReleaseDate.toInstant.toEpochMilli, isWanted = true)
              }

          val knownDrops: List[DropsTable] = MYSQLConnection.selectDrops()

          foundDrops.foreach { foundDrop: DropsTable =>
            if (knownDrops.exists(_.url == foundDrop.url))
              println("eastbay drop is already found so skipping insert")
            else {
              println("eastbay drop found inserting")
              val id: String = UUID.randomUUID().toString
              MYSQLConnection.insertDrop(id, foundDrop.name, foundDrop.color, foundDrop.url, foundDrop.dateTime, isWanted = true)
              context.system.actorOf(Props(classOf[EastbayDropMonitor], id, foundDrop.url, foundDrop.dateTime))
            }
          }

          println("eastbay release monitor complete sleeping for a few hours")
          context.system.scheduler.scheduleOnce(4.hours)(self ! ConnectToWebDriver)
          webDriver.quit()
      }
  }

  override def preStart(): Unit = {
    println("eastbay release date monitor starting")
    self ! ConnectToWebDriver
  }

  case object RecordProductCards

  override def postRestart(reason: Throwable): Unit = {
    println("an exception happened so lets just stop and figure out why ok :)")
    context.stop(self)
  }

}
