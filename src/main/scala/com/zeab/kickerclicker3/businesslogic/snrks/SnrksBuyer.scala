package com.zeab.kickerclicker3.businesslogic.snrks

import akka.actor.Actor
import com.zeab.kickerclicker3.app.appconf.AppConf
import com.zeab.kickerclicker3.app.selenium.{ConnectToWebDriver, GetUrl, Refresh, Selenium}
import com.zeab.kickerclicker3.app.util.ThreadLocalRandom
import org.openqa.selenium.{By, WebElement}
import org.openqa.selenium.remote.RemoteWebDriver

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success, Try}

class SnrksBuyer(id: String, url: String, email: String, password: String, cv: String) extends Actor{

  def screenShotDir: String =
    s"${AppConf.seleniumScreenShotDir}/$id/$email/${System.currentTimeMillis()}.png"

  implicit val ec: ExecutionContext = context.system.dispatcher

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
      Selenium.takeScreenshot(webDriver, screenShotDir)
      context.become(lookForBuyingContainer(webDriver, 0, 0))
      self ! LookForBuyingContainer
  }

  def lookForBuyingContainer(webDriver: RemoteWebDriver, retryCount: Int, refreshCount: Int): Receive = {
    case LookForBuyingContainer =>
      Try(webDriver.findElement(By.xpath("//div[contains(@class, 'buying-tools-container')]"))) match {
        case Failure(exception: Throwable) =>
          Selenium.takeScreenshot(webDriver, screenShotDir)
          println(exception.toString)
        case Success(buyingContainer: WebElement) =>
          println(s"$email:$url found the buying container")
          Selenium.takeScreenshot(webDriver, screenShotDir)
          val foundButtons: List[WebElement] = buyingContainer.findElements(By.tagName("button")).asScala.toList
          foundButtons.size match {
            case 0 =>
              context.become(lookForSoldOut(buyingContainer, webDriver, retryCount, refreshCount))
              self ! LookForSoldOut
            case 1 =>
              context.become(lookForNotifyMe(buyingContainer, webDriver, retryCount, refreshCount))
              self ! LookForNotifyMe
            case _ =>
              context.become(lookForAvailableSizes(foundButtons, webDriver, retryCount, refreshCount))
              self ! LookForAvailableSizes
          }
      }
  }

  def lookForSoldOut(buyingContainer: WebElement, webDriver: RemoteWebDriver, retryCount: Int, refreshCount: Int): Receive = {
    case LookForSoldOut =>
      Try(buyingContainer.findElement(By.xpath(s".//div[contains(.,'Sold Out')]"))) match {
        case Failure(exception: Throwable) =>
          println(exception.toString)
          Selenium.takeScreenshot(webDriver, screenShotDir)
          if (retryCount + 1 <= 5) {
            context.become(lookForBuyingContainer(webDriver, retryCount + 1, refreshCount))
            context.system.scheduler.scheduleOnce(2.second)(self ! LookForBuyingContainer)
          }
          else if (refreshCount + 1 == 5){
            context.become(refresh(webDriver, retryCount, refreshCount))
            context.system.scheduler.scheduleOnce(2.second)(self ! Refresh)
          }
          else {
            println("we don't know whats happening at all so closing up shop")
            webDriver.quit()
            context.stop(self)
          }
        case Success(_) =>
          println("looks to be sold out sorry :(")
          Selenium.takeScreenshot(webDriver, screenShotDir)
          webDriver.quit()
          context.stop(self)
      }
  }

  def lookForNotifyMe(buyingContainer: WebElement, webDriver: RemoteWebDriver, retryCount: Int, refreshCount: Int): Receive = {
    case LookForNotifyMe =>
      Try(buyingContainer.findElement(By.xpath(s".//button[contains(.,'Notify Me')]"))) match {
        case Failure(exception) =>
          println(exception.toString)
          context.become(lookForBuyingContainer(webDriver, retryCount, refreshCount))
          context.system.scheduler.scheduleOnce(2.second)(self ! LookForBuyingContainer)
        case Success(_) =>
          println("found notify me")
          context.become(lookForBuyingContainer(webDriver, retryCount, refreshCount))
          context.system.scheduler.scheduleOnce(2.second)(self ! LookForBuyingContainer)
      }
  }

  def lookForAvailableSizes(possibleSizes: List[WebElement], webDriver: RemoteWebDriver, retryCount: Int, refreshCount: Int): Receive = {
    case LookForAvailableSizes =>
      //Find all the valid sizes
      val availableSizes: List[WebElement] = possibleSizes.filter((button: WebElement) => button.isEnabled).filterNot(_.getText == "ADD TO CART").filterNot(_.getText.contains("Enter Drawing")).filterNot(_.getText.contains("BUY"))
      //Pick and click on the top size
      val selectedSize: WebElement = ThreadLocalRandom.getRandomItemFromCollection(availableSizes)
      println(s"clicking size: ${selectedSize.getText}")
      selectedSize.click()
      Selenium.takeScreenshot(webDriver, screenShotDir)

      //Find the other possible buttons on the list
      val addToCart: Option[WebElement] = possibleSizes.find(_.getText == "ADD TO CART")
      val enterDrawing: Option[WebElement] = possibleSizes.find(_.getText == "Enter Drawing")

      //What to do if those buttons are found so we know how to proceed
      (addToCart, enterDrawing) match {
        case (Some(cart: WebElement), None) =>
          println("clicking add to cart")
          cart.click()
        case (None, Some(draw: WebElement)) =>
          println("clicking enter drawing")
          draw.click()
        case (None, None) =>
          println("cant find either the add to cart or enter drawing buttons")
      }
      Thread.sleep(4000)
      Selenium.takeScreenshot(webDriver, screenShotDir)
  }

  def lookForEnterDrawing(webDriver: RemoteWebDriver): Receive = {
    case _ =>
  }

  def lookForAddToCart(webDriver: RemoteWebDriver): Receive = {
    case _ =>
  }

  def refresh(webDriver: RemoteWebDriver, retryCount: Int, refreshCount: Int): Receive ={
    case Refresh =>
      println("refreshing nav page because we don't know where we are")
      webDriver.navigate().refresh()
      context.become(lookForBuyingContainer(webDriver, retryCount, refreshCount + 1))
  }

  override def preStart(): Unit = {
    println(s"$email:$url starting the buy")
    self ! ConnectToWebDriver
  }

  case object LookForBuyingContainer

  case object LookForSoldOut

  case object LookForNotifyMe

  case object LookForAvailableSizes

}
