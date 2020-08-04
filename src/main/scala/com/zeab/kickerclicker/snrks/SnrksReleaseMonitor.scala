//package com.zeab.kickerclicker.snrks
//
//import java.time.format.DateTimeFormatter
//import java.time.{LocalDate, ZoneId}
//import java.util.{Locale, UUID}
//
//import akka.actor.SupervisorStrategy.Stop
//import akka.actor.{Actor, OneForOneStrategy, SupervisorStrategy}
//import com.zeab.kickerclicker.selenium.Selenium
//import com.zeab.kickerclicker2.sqlconnection.SQLConnection
//import org.openqa.selenium.remote.RemoteWebDriver
//import org.openqa.selenium.{By, WebElement}
//
//import scala.collection.JavaConverters._
//import scala.concurrent.ExecutionContext
//import scala.concurrent.duration._
//import scala.util.{Failure, Success, Try}
//
//class SnrksReleaseMonitor extends Actor{
//
//  implicit val ec: ExecutionContext = context.system.dispatcher
//
//  override val supervisorStrategy: SupervisorStrategy =
//    OneForOneStrategy(maxNrOfRetries = 1, withinTimeRange = 1.minute) {
//      case _ => Stop
//    }
//
//  val url = "https://www.nike.com/launch?s=upcoming"
//
//  def receive: Receive = {
//    case Start =>
//      Selenium.firefox("192.168.1.144", "4440") match {
//        case Failure(exception: Throwable) =>
//          context.stop(self)
//        case Success(webDriver: RemoteWebDriver) =>
//          webDriver.manage().window().maximize()
//          webDriver.get(url)
//          Try(webDriver.findElements(By.xpath(s"//div[contains(@class,'product-card')]"))) match {
//            case Failure(exception) =>
//              println(exception)
//            case Success(foundProductCards) =>
//              val releaseDrops = foundProductCards.asScala.toList.map{ productCard =>
//                val text = productCard.getText.split("\n")
//                if (text.isEmpty) ("", "", "", "")
//                else {
//                  val month = text(0).toLowerCase()
//                  val date = month.charAt(0).toUpper + month.substring(1) + " " + text(1)
//                  val formatter = DateTimeFormatter.ofPattern("MMM d yyyy", Locale.US)
//                  val localDate = LocalDate.parse(date + " 2020", formatter)
//                  val actualReleaseDate = localDate.atTime(7, 0).atZone( ZoneId.of("America/Los_Angeles"))
//                  val name = text(2)
//                  val color = text(3)
//                  val url =
//                    Try(productCard.findElement(By.xpath(s".//a[contains(@class,'card-link')]"))) match {
//                      case Failure(exception) =>
//                        println()
//                        ""
//                      case Success(ggg) =>
//                        val url = ggg.getAttribute("href")
//                        url
//                    }
//                  (name, color, url, actualReleaseDate)
//                }
//              }
//
//              val knownDrops = SQLConnection.selectDrops()
//              //TODO This is crazy ugly so ... fix it ...
//              val hrh =
//                releaseDrops.map{releaseInfo =>
//                  val ff =
//                    knownDrops.filter(_.url == releaseInfo._3)
//                  if (ff.isEmpty) {
//                    val id: String = UUID.randomUUID().toString
//                    SQLConnection.insertDrop(id, releaseInfo._1, releaseInfo._2, releaseInfo._3, releaseInfo._4.toString, "1", "0")
//                  }
//                  else println("drop is already found so skipping insert")
//                }
//              webDriver.close()
//          }
//      }
//  }
//
//  override def preStart(): Unit = {
//    self ! Start
//    context.system.scheduler.scheduleOnce(24.hour)(self ! Start)
//  }
//
//  case object Start
//
//  case object GetWebSite
//
//}
//
//
