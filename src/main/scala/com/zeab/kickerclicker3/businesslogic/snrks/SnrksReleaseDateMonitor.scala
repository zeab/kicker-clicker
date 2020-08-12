package com.zeab.kickerclicker3.businesslogic.snrks

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZoneId, ZonedDateTime}
import java.util
import java.util.{Locale, UUID}
import org.openqa.selenium.JavascriptExecutor
import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, OneForOneStrategy, Props, SupervisorStrategy}
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

class SnrksReleaseDateMonitor extends Actor {

  def screenShotDir: String =
    s"${AppConf.seleniumScreenShotDir}/stuff/${System.currentTimeMillis()}.png"

  implicit val ec: ExecutionContext = context.system.dispatcher

  val url: String = "https://www.nike.com/launch?s=upcoming"

  override val supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 0, withinTimeRange = 1.minute) {
      case _ => Stop
    }

  def receive: Receive = connectToWebDriver

  def connectToWebDriver: Receive = {
    case ConnectToWebDriver =>
      Selenium.firefox(AppConf.seleniumRemoteDriverHost, AppConf.seleniumRemoteDriverPort) match {
        case Failure(exception: Throwable) =>
          println(exception.toString)
          context.system.stop(self)
        case Success(webDriver: RemoteWebDriver) =>
          println("snrks release monitor remote driver connected")
          context.become(openReleaseCalendar(webDriver))
          self ! GetUrl
      }
  }

  def openReleaseCalendar(webDriver: RemoteWebDriver): Receive = {
    case GetUrl =>
      println("snrks release monitor getting releases")
      webDriver.manage().window().maximize()
      webDriver.get(url)
      context.become(recordProductCards(webDriver))
      self ! RecordProductCards
  }

  def recordProductCards(webDriver: RemoteWebDriver): Receive = {
    case RecordProductCards =>
      Try(webDriver.findElements(By.xpath(s"//div[contains(@class,'product-card')]"))) match {
        case Failure(exception: Throwable) =>
          println(exception.toString)
          webDriver.quit()
          context.system.stop(self)
        case Success(productCards: util.List[WebElement]) =>
          val foundDrops: List[DropsTable] =
            productCards.asScala.toList
              .filterNot(_.getText == "")
              .map { productCard: WebElement =>
                webDriver.executeScript("arguments[0].scrollIntoView(true);", productCard)
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
                val imageUrl: String =
                  Try(productCard.findElement(By.xpath(s".//img"))) match {
                    case Failure(_) => "https://placeholdit.imgix.net/~text?txtsize=33&txt=318%C3%97180&w=318&h=180"
                    case Success(image: WebElement) => image.getAttribute("src")
                  }
                DropsTable("", name, color, url, imageUrl, actualReleaseDate.toInstant.toEpochMilli, isWanted = true)
              }

          val knownDrops: List[DropsTable] = MYSQLConnection.selectDrops()

          foundDrops.foreach { foundDrop: DropsTable =>
            if (knownDrops.exists(_.url == foundDrop.url))
              println("snrks drop is already found so skipping insert")
            else {
              println("snrks drop found inserting")
              val id: String = UUID.randomUUID().toString
              MYSQLConnection.insertDrop(id, foundDrop.name, foundDrop.color, foundDrop.url, foundDrop.imageUrl, foundDrop.dateTime, isWanted = true)
              context.system.actorOf(Props(classOf[SnrksDropMonitor], id, foundDrop.url, foundDrop.dateTime))
            }
          }
          println("snrks release monitor complete sleeping for a few hours")
          context.become(connectToWebDriver)
          context.system.scheduler.scheduleOnce(5.minute)(self ! ConnectToWebDriver)
          webDriver.quit()
      }
  }

  override def preStart(): Unit = {
    println("snrks release date monitor starting")
    self ! ConnectToWebDriver
  }

  override def postRestart(reason: Throwable): Unit = {
    println("an exception happened so lets just stop and figure out why ok :)")
    context.stop(self)
  }

  case object RecordProductCards

}
