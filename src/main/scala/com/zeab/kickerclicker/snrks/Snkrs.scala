package com.zeab.kickerclicker.snrks

import java.time.ZonedDateTime

import akka.actor.Actor
import com.zeab.kickerclicker.buyer.Buyer
import com.zeab.kickerclicker.selenium.Selenium
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.{By, WebElement}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class Snkrs(id: String, url: String, dropDateTime: ZonedDateTime) extends Actor with Buyer {

  //TODO Make the clicks safers as well

  implicit val ec: ExecutionContext = context.system.dispatcher

  def receive: Receive = startDriver

  def startDriver: Receive = {
    case Init =>
      Selenium.firefox("192.168.1.144", "4440") match {
        case Failure(exception: Throwable) =>
          println(s"$id-${exception.toString}")
          context.stop(self)
        case Success(webDriver: RemoteWebDriver) =>
          implicit val wb: RemoteWebDriver = webDriver
          println(s"$url $id web driver is successful")
          context.become(openBrowser())
          self ! GetWebSite
      }
  }

  def openBrowser(refreshCount: Int = 0)(implicit webDriver: RemoteWebDriver): Receive = {
    case GetWebSite =>
      webDriver.manage().window().maximize()
      println(s"$url opening website")
      webDriver.get(url)
      self ! WaitForSiteToLoad
    case WaitForSiteToLoad =>
      println(s"refresh = $refreshCount")
      waitUntilPageLoaded(5, By.xpath("//div[contains(@class, 'buying-tools-container')]")) match {
        case Failure(exception) =>
          Selenium.takeScreenshot(id)
          println(exception.toString)
          webDriver.close()
          context.stop(self)
        case Success(element: WebElement) =>
          Selenium.takeScreenshot(id)
          println(s"$url is up and loaded")
          self ! LookForNotifyMe(element)
          context.become(active(refreshCount))
      }
  }

  def notify(watchCount: Int = 0, refreshCount: Int = 0)(implicit webDriver: RemoteWebDriver): Receive = {
    case LookForNotifyMe(buyingContainer: WebElement) =>
      context.become(notify(watchCount + 1))
      if (watchCount == 10) {
        println(s"$url we hit 10 looks at the notify... lets refresh")
        self ! Refresh
      }
      else {
        Try(buyingContainer.findElement(By.xpath(s"//button[contains(.,'Notify Me')]"))) match {
          case Failure(_) =>
            println(s"$url we did not find the notify me button")
            context.become(active(refreshCount))
            self ! LookForSoldOut(buyingContainer)
          case Success(_) =>
            println(s"$url look like its not on sale yet")
            context.system.scheduler.scheduleOnce(1.second)(self ! LookForNotifyMe(buyingContainer))
        }
      }
    case Refresh =>
      if ((refreshCount + 1) == 5){
        println(s"$url closing up shop")
        webDriver.close()
        context.stop(self)
      }
      else{
        println(s"$url refreshing")
        webDriver.navigate().refresh()
        context.become(openBrowser(refreshCount + 1))
        self ! WaitForSiteToLoad
      }
  }

  def active(refreshCount: Int = 0)(implicit webDriver: RemoteWebDriver): Receive = {
    case LookForNotifyMe(buyingContainer: WebElement) =>
      Selenium.takeScreenshot(id)
      context.become(notify(refreshCount = refreshCount))
      self ! LookForNotifyMe(buyingContainer)
    case LookForSoldOut(buyingContainer: WebElement) =>
      Try(buyingContainer.findElement(By.xpath(s"//div[contains(.,'Sold Out')]"))) match {
        case Failure(_) =>
          println(s"$url did not find sold out so there is still hope")
          self ! LookForSizes(buyingContainer)
        case Success(_) =>
          Selenium.takeScreenshot(id)
          println(s"$url looks like its sold out :( shutting down")
          context.stop(self)
      }
    case LookForSizes(buyingContainer: WebElement) =>
      val sizesAvailable: List[WebElement] =
        buyingContainer.findElements(By.tagName("button")).asScala.toList
          .filter((button: WebElement) => button.isEnabled)
      sizesAvailable.lastOption match {
        case Some(size: WebElement) =>
          size.click()
          Selenium.takeScreenshot(id)
          println(s"$url clicking on size ${size.getText}")
          self ! EnterDrawing(buyingContainer)
        case None =>
          Selenium.takeScreenshot(id)
          println(s"$url we cant find any sizes to choose from")
          webDriver.close()
          context.stop(self)
      }
    case EnterDrawing(buyingContainer: WebElement) =>
      Try(buyingContainer.findElement(By.xpath(s"//button[contains(.,'Enter Drawing')]"))) match {
        case Failure(_) =>
          println(s"$url did not find enter drawing button")
          self ! AddToCart(buyingContainer)
        case Success(button: WebElement) =>
          println(s"$url lets enter the drawing")
          button.click()
          Thread.sleep(10000)
          Selenium.takeScreenshot(id)
          context.stop(self)
          println(s"$url were at the end so stopping and screen shotting")
      }
    case AddToCart(buyingContainer: WebElement) =>
      Try(buyingContainer.findElement(By.xpath(s"//div[contains(.,'ADD TO CART')]"))) match {
        case Failure(_) =>
          println(s"$url did not find the add to cart button ... so idk whats up")
        case Success(button: WebElement) =>
          println(s"$url adding to cart")
          button.click()
          Selenium.takeScreenshot(id)
      }
  }

  override def preStart(): Unit = {
    scheduleStart(dropDateTime, self)
  }

  case class LookForSoldOut(element: WebElement)

  case class LookForNotifyMe(element: WebElement)

  case class LookForSizes(element: WebElement)

  case class EnterDrawing(element: WebElement)

  case class AddToCart(element: WebElement)

  case object Refresh

  case object WaitForSiteToLoad

}
