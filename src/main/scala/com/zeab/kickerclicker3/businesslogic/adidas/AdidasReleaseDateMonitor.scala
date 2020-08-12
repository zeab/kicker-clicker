package com.zeab.kickerclicker3.businesslogic.adidas

import java.time.ZonedDateTime
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.util
import java.util.UUID

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

  implicit val ec: ExecutionContext = context.system.dispatcher

  val url: String = "https://www.adidas.com/us/release-dates"

  def receive: Receive = connectToWebDriver

  def connectToWebDriver: Receive = {
    case ConnectToWebDriver =>
      Selenium.firefox(AppConf.seleniumRemoteDriverHost, AppConf.seleniumRemoteDriverPort, loadImages = true) match {
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

          val foundDrops: List[DropsTable] =
            productCards.asScala.toList.map{ productCard: WebElement =>
              webDriver.executeScript("arguments[0].scrollIntoView(true);", productCard)
              val name: String = Try(productCard.findElement(By.xpath(s".//div[contains(@class,'plc-product-name')]"))) match {
                case Failure(exception) => "undefined"
                case Success(productName: WebElement) => productName.getText
              }
              val date: String = Try(productCard.findElement(By.xpath(s".//div[contains(@class,'plc-product-date')]"))) match {
                case Failure(exception) => "undefined"
                case Success(productDate: WebElement) => productDate.getText
              }
              val formatter: DateTimeFormatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("EEEE d MMM h:mm a z yyyy").toFormatter()
              val localDate: Long = ZonedDateTime.parse(date + " 2020", formatter).toInstant.toEpochMilli
              val url: String =
                Try(productCard.findElement(By.xpath(".//a"))) match {
                  case Failure(exception) => "undefined"
                  case Success(cardInfo) => cardInfo.getAttribute("href")
                }
              val imageUrl: String =
                Try(productCard.findElement(By.xpath(s".//img"))) match {
                  case Failure(_) => "https://placeholdit.imgix.net/~text?txtsize=33&txt=318%C3%97180&w=318&h=180"
                  case Success(image: WebElement) => image.getAttribute("src")
                }
              DropsTable("", name, "", url, imageUrl, localDate, isWanted = true)
            }

          val knownDrops: List[DropsTable] = MYSQLConnection.selectDrops()

          foundDrops.foreach { foundDrop: DropsTable =>
            if (knownDrops.exists(_.url == foundDrop.url))
              println("adidas drop is already found so skipping insert")
            else {
              println("adidas drop found inserting")
              val id: String = UUID.randomUUID().toString
              MYSQLConnection.insertDrop(id, foundDrop.name, foundDrop.color, foundDrop.url, foundDrop.imageUrl, foundDrop.dateTime, isWanted = true)
              //context.system.actorOf(Props(classOf[SnrksDropMonitor], id, foundDrop.url, foundDrop.dateTime))
            }
          }

          println("adidas release monitor complete sleeping for a few hours")
          context.system.scheduler.scheduleOnce(4.hour)(self ! ConnectToWebDriver)
          context.become(connectToWebDriver)
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
