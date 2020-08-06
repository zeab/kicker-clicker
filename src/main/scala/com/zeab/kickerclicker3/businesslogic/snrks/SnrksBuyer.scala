package com.zeab.kickerclicker3.businesslogic.snrks

import akka.actor.Actor
import com.zeab.kickerclicker3.app.appconf.AppConf
import com.zeab.kickerclicker3.app.selenium.{ConnectToWebDriver, GetUrl, Selenium}
import org.openqa.selenium.{By, WebElement}
import org.openqa.selenium.remote.RemoteWebDriver

import scala.util.{Failure, Success, Try}

class SnrksBuyer(id: String, url: String, email: String, password: String, cv: String) extends Actor{

  def receive: Receive = connectToWebDriver

  def connectToWebDriver: Receive = {
    case ConnectToWebDriver =>
      Selenium.firefox(AppConf.seleniumRemoteDriverHost, AppConf.seleniumRemoteDriverPort) match {
        case Failure(exception: Throwable) =>
          println(exception.toString)
          context.system.stop(self)
        case Success(webDriver: RemoteWebDriver) =>
          println(s"$email:$url connecting to web driver")
          webDriver.manage().window().maximize()
          context.become(openBuyPage(webDriver))
          self ! GetUrl
      }
  }

  def openBuyPage(webDriver: RemoteWebDriver): Receive = {
    case GetUrl =>
      println(s"$email:$url connecting buy page")
      webDriver.get(url)
      Selenium.takeScreenshot(webDriver, s"${AppConf.seleniumScreenShotDir}/$id/$email/${System.currentTimeMillis()}.png")
      context.become(lookForBuyingContainer(webDriver))
      self ! LookForBuyingContainer
  }

  def lookForBuyingContainer(webDriver: RemoteWebDriver): Receive = {
    case LookForBuyingContainer =>
      Try(webDriver.findElement(By.xpath("//div[contains(@class, 'buying-tools-container')]"))) match {
        case Failure(exception: Throwable) =>
          Selenium.takeScreenshot(webDriver, s"${AppConf.seleniumScreenShotDir}/$id/$email/${System.currentTimeMillis()}.png")
          println(exception.toString)
        case Success(buyingContainer: WebElement) =>
          println(s"$email:$url found the buying container")
          Selenium.takeScreenshot(webDriver, s"${AppConf.seleniumScreenShotDir}/$id/$email/${System.currentTimeMillis()}.png")

          //look for the amount of buttons and then based on that make an educated guess as to where we are at in the process
          //if its 1 then its either notify me
          //if there are 0 then its sold out
          //if there are more then lets look for sizes

          println()
      }
  }

  def lookForSoldOut(webDriver: RemoteWebDriver): Receive = {
    case _ =>
  }

  def lookForNotifyMe(webDriver: RemoteWebDriver): Receive = {
    case _ =>
  }

  def lookForAvailableSizes(webDriver: RemoteWebDriver): Receive = {
    case _ =>
  }

  def lookForEnterDrawing(webDriver: RemoteWebDriver): Receive = {
    case _ =>
  }

  def lookForAddToCart(webDriver: RemoteWebDriver): Receive = {
    case _ =>
  }

  override def preStart(): Unit = {
    println(s"$email:$url starting the buy")
    self ! ConnectToWebDriver
  }

  case object LookForBuyingContainer

}
