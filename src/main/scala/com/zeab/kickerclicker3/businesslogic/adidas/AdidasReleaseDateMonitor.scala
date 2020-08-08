package com.zeab.kickerclicker3.businesslogic.adidas

import java.text.DateFormatSymbols
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util
import java.util.{Calendar, Locale, UUID}

import akka.actor.Actor
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
class AdidasReleaseDateMonitor extends Actor {

  val dfs = new DateFormatSymbols
  val weekdays = dfs.getWeekdays.filterNot(_ == "").toList

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

          val foundDrops =
            productCards.asScala.toList.map{ productCard: WebElement =>
              Try(productCard.findElement(By.xpath(s".//div[contains(@class,'gl-product-card')]"))) match {
                case Failure(exception) =>
                  println(exception.toString)
                  DropsTable("", "", "", "", 0, isWanted = false)
                case Success(innerProductCard: WebElement) =>
                  val cal = Calendar.getInstance
                  val year = cal.get(Calendar.YEAR)
                  val releaseDate1 =
                    innerProductCard.getText.split('\n').toList
                      .find(s => weekdays.map(_.toUpperCase).exists(s.contains)).getOrElse("")
                      .map(_.toLower)
                      .split(' ')
                      .map{s =>
                        if (s == "") ""
                        else s.charAt(0).toUpper + s.substring(1)
                      }
                      .map{s =>
                        if (s == "Am") "AM"
                        else if (s =="Pm") "PM"
                        else s
                      }
                      .map{s =>
                        if (s == "Utc") "UTC"
                        else s
                      }
                      .map{s =>
                        if (s.contains(':'))
                          if (s.length == 4) "0" + s
                          else s
                        else s
                      }
                      .mkString(" ") + s" $year"

                  val realReleaseDate =
                    if (releaseDate1 == "") 0
                    else {
                      val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE d MMM hh:mm a z yyyy", Locale.US)
                      val localDate = ZonedDateTime.parse(releaseDate1, formatter).toInstant.toEpochMilli
                      localDate
                    }

                  Try(innerProductCard.findElement(By.xpath(".//a"))) match {
                    case Failure(exception) =>
                      println(exception.toString)
                      DropsTable("", "", "", "", 0, isWanted = false)
                    case Success(cardInfo) =>
                      val url = cardInfo.getAttribute("href")
                      DropsTable("", "", "", url, realReleaseDate, isWanted = true)
                  }
              }
            }.filterNot(_.url == "")

          val knownDrops: List[DropsTable] = MYSQLConnection.selectDrops()

          foundDrops.foreach { foundDrop: DropsTable =>
            if (knownDrops.exists(_.url == foundDrop.url))
              println("adidas drop is already found so skipping insert")
            else {
              println("adidas drop found inserting")
              val id: String = UUID.randomUUID().toString
              MYSQLConnection.insertDrop(id, foundDrop.name, foundDrop.color, foundDrop.url, foundDrop.dateTime, isWanted = true)
              //context.system.actorOf(Props(classOf[SnrksDropMonitor], id, foundDrop.url, foundDrop.dateTime))
            }
          }

          println("adidas release monitor complete sleeping for a few hours")
          context.system.scheduler.scheduleOnce(4.hours)(self ! ConnectToWebDriver)
          webDriver.quit()
      }
  }

  override def preStart(): Unit = {
    println("adidas release date monitor starting")
    self ! ConnectToWebDriver
  }

  case object RecordProductCards

  override def postRestart(reason: Throwable): Unit = {
    println("an exception happened so lets just stop and figure out why ok :)")
    context.stop(self)
  }

}
