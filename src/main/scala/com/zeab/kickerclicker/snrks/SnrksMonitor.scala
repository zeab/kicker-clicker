package com.zeab.kickerclicker.snrks

import java.awt.Robot
import java.awt.event.KeyEvent

import akka.actor.Actor
import com.zeab.kickerclicker.utilities.ThreadLocalRandom
import org.openqa.selenium.firefox.{FirefoxDriver, FirefoxOptions}
import org.openqa.selenium.{By, WebDriver, WebElement}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class SnrksMonitor(name: String, size: String, isMale: Boolean, skipLogin: Boolean) extends Actor {

  //TODO Add some more validation along the way like making sure im actually on the page and step that i actually think im on

  implicit val ec: ExecutionContext = context.system.dispatcher

  //TODO Need to hook these up correctly
  val username: String = System.getenv("EMAIL")
  val password: String = System.getenv("PASSWORD")

  //Set Firefox Headless mode as TRUE
  val options: FirefoxOptions = new FirefoxOptions
  //options.setHeadless(true)
  val firefox: WebDriver = new FirefoxDriver(options) //new RemoteWebDriver(new URL(s"http://192.168.1.144:4444/wd/hub"), options)
  val url: String = s"https://www.nike.com/launch/t/$name"
  firefox.get(url)

  val robot: Robot = new Robot()
  println("About to zoom out")
  for (i <- 0 until 5) {
    robot.keyPress(KeyEvent.VK_CONTROL)
    robot.keyPress(KeyEvent.VK_SUBTRACT)
    robot.keyRelease(KeyEvent.VK_SUBTRACT)
    robot.keyRelease(KeyEvent.VK_CONTROL)
  }

  val maleOrFemale: String =
    if (isMale) "M"
    else "W"

  override def receive: Receive = {
    case Login =>
      buttonClick(
        "//button[contains(.,'Join / Log In')]",
        Login,
        EnterLoginName,
        "unable to find login button",
        "clicking the login button"
      )
    case EnterLoginName =>
      input(
        username,
        "//input[@placeholder='Email address']",
        EnterLoginName,
        EnterLoginPassword,
        "unable to find enter login info",
        "entering login info"
      )
    case EnterLoginPassword =>
      input(
        password,
        "//input[@placeholder='Password']",
        EnterLoginPassword,
        SignIn,
        "unable to find enter password info",
        "entering password"
      )
    case SignIn =>
      Try(firefox.findElement(By.xpath("//*[contains(@value,'SIGN IN')]"))) match {
        case Failure(exception) =>
          context.system.scheduler.scheduleOnce(4.second)(self ! SignIn)
          println("unable to find signin button")
        case Success(button) =>
          println("entering signin button")
          button.click()
          context.system.scheduler.scheduleOnce(4000.milli)(self ! LookForSize)
      }
    case LookForSize =>
      buttonClick(
        s"//button[contains(.,'$maleOrFemale $size')]",
        LookForSize,
        AddToCart,
        "unable to find size button",
        "clicking the size button"
      )
    case AddToCart =>
      buttonClick(
        "//button[contains(.,'ADD TO CART')]",
        AddToCart,
        GoToCart,
        "unable to find add to cart button",
        "clicking the add to cart button"
      )
    case GoToCart =>
      buttonClick(
        "/html/body/div[2]/div/div/div[1]/div/header/div[1]/section/ul/li[3]/a/i",
        GoToCart,
        Checkout,
        "unable to find go to cart button",
        "clicking the go to cart button"
      )
    case Checkout =>
      buttonClick(
        "//button[contains(.,'Checkout')]",
        Checkout,
        SubmitOrder,
        "unable to find checkout button",
        "clicking the checkout button"
      )
    case SubmitOrder =>
      Try(firefox.findElement(By.xpath("//button[contains(.,'Submit Order')]"))) match {
        case Failure(exception) =>
          context.system.scheduler.scheduleOnce(4.second)(self ! SubmitOrder)
          println("unable to find submit order button")
        case Success(button) =>
          println("clicking the submit order button")
        //button.click()
      }
  }

  def buttonClick[A, B](
                         searchString: String,
                         retry: A,
                         continue: B,
                         failureMessage: String,
                         successMessage: String,
                         failureWait: Int = 4,
                         successWaitMax: Int = 1000,
                         successWaitMin: Int = 500
                       ): Unit =
    Try(firefox.findElement(By.xpath(searchString))) match {
      case Failure(exception: Throwable) =>
        context.system.log.debug(exception.toString)
        println(failureMessage)
        context.system.scheduler.scheduleOnce(failureWait.second)(self ! retry)
      case Success(button: WebElement) =>
        println(successMessage)
        Try(button.click()) match {
          case Failure(exception: Throwable) =>
            context.system.log.debug(exception.toString)
            println(failureMessage)
          case Success(_) =>
            context.system.scheduler.scheduleOnce(ThreadLocalRandom.getRandomInt(successWaitMax, successWaitMin).milli)(self ! continue)
        }
    }

  def input[A, B](
                   input: String,
                   searchString: String,
                   retry: A,
                   continue: B,
                   failureMessage: String,
                   successMessage: String,
                   failureWait: Int = 4,
                   successWaitMax: Int = 1000,
                   successWaitMin: Int = 500
                 ): Unit =
    Try(firefox.findElement(By.xpath(searchString))) match {
      case Failure(exception: Throwable) =>
        context.system.log.error(exception.toString)
        println(failureMessage)
        context.system.scheduler.scheduleOnce(failureWait.second)(self ! retry)
      case Success(inputArea: WebElement) =>
        println(successMessage)
        Try(inputArea.sendKeys(input)) match {
          case Failure(exception: Throwable) =>
            context.system.log.error(exception.toString)
            println(failureMessage)
          case Success(_) =>
            context.system.scheduler.scheduleOnce(ThreadLocalRandom.getRandomInt(successWaitMax, successWaitMin).milli)(self ! continue)
        }
    }

  override def preStart(): Unit = {
    if (skipLogin) self ! LookForSize
    else self ! Login
  }

  case object SubmitOrder

  case object SignIn

  case object EnterLoginName

  case object EnterLoginPassword

  case object Checkout

  case object GoToCart

  case object AddToCart

  case object LookForSize

  case object FindNotifyButton

  case object Login

}
