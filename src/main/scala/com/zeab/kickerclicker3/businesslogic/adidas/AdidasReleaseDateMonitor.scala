package com.zeab.kickerclicker3.businesslogic.adidas

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
import java.text.DateFormatSymbols
class AdidasReleaseDateMonitor extends Actor {

  implicit val ec: ExecutionContext = context.system.dispatcher

  val url: String = "https://www.adidas.com/us/release-dates"

  def receive: Receive = connectToWebDriver

  def connectToWebDriver: Receive = {
    case ConnectToWebDriver =>
      Selenium.firefox(AppConf.seleniumRemoteDriverHost, AppConf.seleniumRemoteDriverPort) match {
        case Failure(exception: Throwable) =>
          println(exception.toString)
          context.system.stop(self)
        case Success(webDriver: RemoteWebDriver) =>
          println("adidas release monitor remote driver connected")
          context.become(openReleaseCalendar(webDriver))
          self ! GetUrl
      }
  }

  def openReleaseCalendar(webDriver: RemoteWebDriver): Receive = {
    case GetUrl =>
      println("adidas release monitor getting releases")
      webDriver.manage().window().maximize()
      webDriver.get(url)
      context.become(recordProductCards(webDriver))
      self ! RecordProductCards
  }

  def recordProductCards(webDriver: RemoteWebDriver): Receive = {
    case RecordProductCards =>
      Try(webDriver.findElements(By.xpath(s"//div[contains(@class,'plc-product-card')]"))) match {
        case Failure(exception: Throwable) =>
          println(exception.toString)
          webDriver.quit()
          context.system.stop(self)
        case Success(productCards: util.List[WebElement]) =>

          val ff =
            productCards.asScala.toList.map{ card =>
              Try(card.findElement(By.xpath(s".//div[contains(@class,'gl-product-card')]"))) match {
                case Failure(exception) =>
                  println(exception.toString)
                  ("", "")
                case Success(innerCard) =>
                  val dfs = new DateFormatSymbols
                  val weekdays = dfs.getWeekdays.map(_.toLowerCase()).filterNot(_ == "").toList
                  val text = innerCard.getText.split('\n').map(_.toLowerCase()).toList
                  val releaseDate = text.find(s => weekdays.exists(s.contains)).getOrElse("")
                  val gg: String =
                  Try(innerCard.findElement(By.xpath(".//a"))) match {
                    case Failure(exception) =>
                      "cant find url"
                    case Success(value) =>
                      val kk = value.getAttribute("href")
                      val oo = value.getText
                      kk
                  }
                  (gg, releaseDate)
              }
            }

          val ttt = ff.filterNot(_._2 == "")

          println()

          val jj = productCards.asScala.toList
            .filterNot(_.getText == "")

          val kk = jj.map(_.getText)

          println()
          val foundDrops: List[DropsTable] =
            productCards.asScala.toList
              .filterNot(_.getText == "")
              .map { productCard: WebElement =>
                val text: Array[String] = productCard.getText.split("\n")
                val month: String = text(0).toLowerCase()
                val date: String = month.charAt(0).toUpper + month.substring(1) + " " + text(1)
                val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d yyyy", Locale.US)
                val localDate: LocalDate = LocalDate.parse(date + " 2020", formatter)
                val actualReleaseDate: ZonedDateTime = localDate.atTime(7, 0).atZone(ZoneId.of("America/Los_Angeles"))
                val name: String = text(2)
                val color: String = text(3)
                val url: String =
                  Try(productCard.findElement(By.xpath(s".//a[contains(@class,'card-link')]"))) match {
                    case Failure(_) => "can not find card link"
                    case Success(cardLink: WebElement) => cardLink.getAttribute("href")
                  }
                DropsTable("", name, color, url, actualReleaseDate.toInstant.getEpochSecond, isWanted = true)
              }

          val knownDrops: List[DropsTable] = MYSQLConnection.selectDrops()

          foundDrops.foreach { foundDrop: DropsTable =>
            if (knownDrops.exists(_.url == foundDrop.url))
              println("snrks drop is already found so skipping insert")
            else {
              println("snrks drop found inserting")
              val id: String = UUID.randomUUID().toString
              MYSQLConnection.insertDrop(id, foundDrop.name, foundDrop.color, foundDrop.url, foundDrop.dateTime, isWanted = true)
              //context.system.actorOf(Props(classOf[SnrksDropMonitor], id, foundDrop.url, foundDrop.dateTime))
            }
          }

          println("snrks release monitor complete sleeping for a few hours")
          context.system.scheduler.scheduleOnce(4.hours)(self ! ConnectToWebDriver)
          webDriver.quit()
      }
  }

  override def preStart(): Unit = {
    println("snrks release date monitor starting")
    self ! ConnectToWebDriver
  }

  case object RecordProductCards

  override def postRestart(reason: Throwable): Unit = {
    println("an exception happened so lets just stop and figure out why ok :)")
    context.stop(self)
  }

}
