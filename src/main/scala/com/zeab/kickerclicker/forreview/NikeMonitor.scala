//package com.zeab.shoes
//
//import java.awt.Robot
//import java.awt.event.KeyEvent
//import java.net.URL
//
//import akka.actor.Actor
//import org.openqa.selenium.{By, WebDriver, WebElement}
//import org.openqa.selenium.firefox.{FirefoxDriver, FirefoxOptions}
//import org.openqa.selenium.remote.RemoteWebDriver
//
//import scala.concurrent.duration._
//import scala.concurrent.ExecutionContext
//import scala.util.{Failure, Success, Try}
//
//class NikeMonitor extends Actor {
//
//  implicit val ec: ExecutionContext = context.system.dispatcher
//
//  //Set Firefox Headless mode as TRUE
//  val options = new FirefoxOptions
//  //options.setHeadless(true)
//  val firefox: WebDriver = new RemoteWebDriver(new URL("http://192.168.1.144:4444/wd/hub"), options)//new FirefoxDriver(options)
//
//  //new RemoteWebDriver(new URL("http://192.168.1.144:4444/wd/hub"), options)
//
//  val url: String = s"https://www.nike.com/t/air-jordan-3-retro-se-shoe-gt1c9k/CV3583-003"
//  println(url)
//  firefox.get(url)
//
//  Thread.sleep(4000)
//  val robot = new Robot()
//  System.out.println("About to zoom out")
//  for (i <- 0 until 5) {
//    robot.keyPress(KeyEvent.VK_CONTROL)
//    robot.keyPress(KeyEvent.VK_SUBTRACT)
//    robot.keyRelease(KeyEvent.VK_SUBTRACT)
//    robot.keyRelease(KeyEvent.VK_CONTROL)
//  }
//
//  Thread.sleep(4000)
//
//  def receive: Receive = {
//    case Refresh =>
//      println("refreshing")
//      firefox.navigate().refresh()
//    case FindSize =>
//      Thread.sleep(3000)
//      Try(firefox.findElement(By.xpath("/html/body/div[2]/div[2]/div/div/div[3]/div[3]/div[2]/div/div/form/div[1]/fieldset/div/div[14]/label"))) match {
//        case Failure(exception) =>
//          println("cant find size button trying again")
//          self ! Refresh
//          val millisecondsToWait: Int = ThreadLocalRandom.getRandomInt(10000, 9900)
//          context.system.scheduler.scheduleOnce(millisecondsToWait.millisecond)(self ! FindSize)
//        case Success(foundButton) =>
//          foundButton.click()
//          self ! FindCheckout
//      }
//    case FindCheckout =>
//      Thread.sleep(3000)
//      Try(firefox.findElement(By.xpath("/html/body/div[2]/div[2]/div/div/div[3]/div[3]/div[2]/div/div/form/div[2]/button[1]"))) match {
//        case Failure(exception) =>
//          println("cant find first button during check out trying another")
//          Try(firefox.findElement(By.xpath("/html/body/div[2]/div[2]/div/div/div[3]/div[3]/div[2]/div/div/form/div[3]/button[1]"))) match {
//            case Failure(exception) =>
//              println("cant find 2nd button either during find check out")
//              context.system.scheduler.scheduleOnce(500.millisecond)(self ! FindCheckout)
//            case Success(foundButton) =>
//              foundButton.click()
//              self ! GoToCart
//          }
//        case Success(foundButton) =>
//          foundButton.click()
//          self ! GoToCart
//      }
//    case GoToCart =>
//      Thread.sleep(3000)
//      Try(firefox.findElement(By.xpath("/html/body/div[2]/div[1]/div[2]/header/nav[1]/section[1]/div/div/ul[2]/li[3]/div/a/i"))) match {
//        case Failure(exception) =>
//          println("cant find checkout button trying again during go to cart")
//          context.system.scheduler.scheduleOnce(500.millisecond)(self ! GoToCart)
//        case Success(foundButton: WebElement) =>
//          foundButton.click()
//          self ! YesGoToCart
//      }
//    case YesGoToCart =>
//      Thread.sleep(3000)
//      Try(firefox.findElement(By.xpath("/html/body/div[2]/div/div[1]/main/div[2]/div[2]/aside/div[7]/div/button[1]"))) match {
//        case Failure(exception) =>
//          println("cant find checkout button trying again during yes i want to check out")
//          context.system.scheduler.scheduleOnce(500.millisecond)(self ! YesGoToCart)
//        case Success(foundButton) =>
//          foundButton.click()
//          println("end of the line")
//      }
//  }
//
//  override def preStart(): Unit = {
//    self ! FindSize
//  }
//
//  case object YesGoToCart
//  case object GoToCart
//  case object FindCheckout
//  case object Refresh
//  case object FindSize
//}
