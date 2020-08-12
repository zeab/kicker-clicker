package com.zeab.kickerclicker3.businesslogic.bodega

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

class BodegaReleaseDateMonitor extends Actor {

  implicit val ec: ExecutionContext = context.system.dispatcher

  val url: String = "https://bdgastore.com/blogs/upcoming-releases"

  def receive: Receive = connectToWebDriver

  def connectToWebDriver: Receive = {
    case ConnectToWebDriver =>
      Selenium.firefox(AppConf.seleniumRemoteDriverHost, AppConf.seleniumRemoteDriverPort) match {
        case Failure(exception: Throwable) =>
          println(exception.toString)
          context.system.stop(self)
        case Success(webDriver: RemoteWebDriver) =>
          println("bodega release monitor remote driver connected")
          context.become(openReleaseCalendar(webDriver))
          self ! GetUrl
      }
  }

  def openReleaseCalendar(webDriver: RemoteWebDriver): Receive = {
    case GetUrl =>
      println("bodega release monitor getting releases")
      webDriver.manage().window().maximize()
      webDriver.get(url)
      context.become(findBlogContainer(webDriver))
      self ! RecordProductCards
  }

  def findBlogContainer(webDriver: RemoteWebDriver): Receive = {
    case RecordProductCards =>
      Try(webDriver.findElement(By.xpath(s"//ul[contains(@class,'row blog-container')]"))) match {
        case Failure(exception: Throwable) =>
          println(exception.toString)
          webDriver.quit()
          context.system.stop(self)
        case Success(blogContainer: WebElement) =>
          Try(blogContainer.findElements(By.xpath(s".//li[contains(@class,'columns')]"))) match {
            case Failure(exception: Throwable) =>
              println(exception.toString)
              webDriver.quit()
              context.system.stop(self)
            case Success(productCards: util.List[WebElement]) =>
              val dd = productCards.asScala.toList.filterNot(_.getText == "")
              val jj = dd.map(_.getText).sorted
              val yy = dd.map(_.getAttribute("href"))
              println()
//                          .map { productCard: WebElement =>
//
//                          }

          }



          println()
          val foundDrops: List[DropsTable] = List.empty
//            productCards.asScala.toList
//              .filterNot(_.getText == "")
//              .map { productCard: WebElement =>
//                val text: Array[String] = productCard.getText.split("\n")
//                val month: String = text(0).toLowerCase()
//                val date: String = month.charAt(0).toUpper + month.substring(1) + " " + text(1)
//                val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d yyyy", Locale.US)
//                val localDate: LocalDate = LocalDate.parse(date + " 2020", formatter)
//                val actualReleaseDate: ZonedDateTime = localDate.atTime(7, 0).atZone(ZoneId.of("America/Los_Angeles"))
//                val name: String = text(2)
//                val color: String = text(3)
//                val url: String =
//                  Try(productCard.findElement(By.xpath(s".//a[contains(@class,'card-link')]"))) match {
//                    case Failure(_) => "can not find card link"
//                    case Success(cardLink: WebElement) => cardLink.getAttribute("href")
//                  }
//                DropsTable("", name, color, url, actualReleaseDate.toString, "1", "0")
//              }

          val knownDrops: List[DropsTable] = MYSQLConnection.selectDrops()

          foundDrops.foreach { foundDrop: DropsTable =>
            if (knownDrops.exists(_.url == foundDrop.url))
              println("snrks drop is already found so skipping insert")
            else {
              println("snrks drop found inserting")
              val id: String = UUID.randomUUID().toString
              MYSQLConnection.insertDrop(id, foundDrop.name, foundDrop.color, foundDrop.url, foundDrop.imageUrl, foundDrop.dateTime, isWanted = true)
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
