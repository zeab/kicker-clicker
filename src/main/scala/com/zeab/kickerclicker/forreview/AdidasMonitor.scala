//package com.zeab.shoes
//
//import java.net.URL
//
//import akka.actor.Actor
//import org.openqa.selenium.firefox.{FirefoxDriver, FirefoxOptions}
//import org.openqa.selenium.remote.RemoteWebDriver
//import org.openqa.selenium.{By, Proxy => P, WebDriver, WebElement}
//
//import scala.concurrent.ExecutionContext
//import scala.concurrent.duration._
//import scala.util.{Failure, Success, Try}
//
//class AdidasMonitor(name: String, sku: String, size: String, port: String) extends Actor {
//
//  //TODO Need an abort feature that says if im doing something to often i should just give up and start again
//  //or should i try and figure out where i am and attempt to finish ... maybe if im near the end or something? I should try but else just restart...
//  //TODO Do I want an auto login in feature? where it will make sure im logged in before i check out?
//
//  implicit val ec: ExecutionContext = context.system.dispatcher
//
//  val convertedSize: String =
//    size match {
//      case "7" => "590"
//      case "7.5" => "600"
//      case "8" => "610"
//      case "8.5" => "620"
//      case "9" => "630"
//      case "9.5" => "640"
//      case "10" => "650"
//      case "10.5" => "660"
//      case "11" => "670"
//    }
//
//  //Set Firefox Headless mode as TRUE
//  val options = new FirefoxOptions
//  //options.setHeadless(true)
//  val firefox: WebDriver = new FirefoxDriver(options)
//  //new RemoteWebDriver(new URL(s"http://192.168.1.144:$port/wd/hub"), options)
//
//  val url: String = s"https://www.adidas.com/us/$name/$sku.html?forceSelSize=${sku}_$convertedSize"
//  println(url)
//  firefox.get(url)
//
//  def receive: Receive = {
//    case Refresh =>
//      println("refreshing")
//      firefox.navigate().refresh()
//    case Poll =>
//      println("trying to find the add to bag button")
//      Try(firefox.findElement(By.xpath("/html/body/div[2]/div/div/div/div/div[3]/div[2]/div[2]/section/div[3]/button"))) match {
//        case Failure(exception: Throwable) =>
//          //println(exception.toString)
//          val millisecondsToWait: Int = ThreadLocalRandom.getRandomInt(5000, 2500)
//          println(s"no add to bag found refreshing and waiting $millisecondsToWait milliseconds")
//          context.system.scheduler.scheduleOnce(millisecondsToWait.millisecond) { self ! Poll }
//          self ! Refresh
//        case Success(foundButton: WebElement) =>
//          Thread.sleep(500)
//          println("add to bag found moving on to the next step")
//          //Add the item to the bag
//          foundButton.click()
//          self ! CheckForPopUp
//      }
//    case CheckForPopUp =>
//      //Click the checkout button on the popup
//      println("trying to click on the check out button")
//      Try(firefox.findElement(By.xpath("/html/body/div[3]/div/div/div/div[2]/div/section/div[3]/a[2]"))) match {
//        case Failure(exception: Throwable) =>
//          println("pop up failed to be found waiting just a few and trying again")
//          context.system.scheduler.scheduleOnce(500.millisecond){ self ! CheckForPopUp }
//        case Success(foundButton: WebElement) =>
//          println("found the pop up button to check out so im clicking it")
//          foundButton.click()
//          println("ok we should be trying to check out now")
//          self ! CheckOut
//      }
//    case CheckOut =>
//      println("I need to finish checking out here")
//      System.exit(0)
//  }
//
//  override def preStart(): Unit = {
//    self ! Poll
//  }
//
//  case object Poll
//
//  case object Refresh
//
//  case object CheckForPopUp
//
//  case object CheckOut
//
//}
//
//
////Steps
////load the page and see if its available
////yes then proceed
////no then refresh the page
//
////if the page is loaded then try and find the add to bag and click
////wait for the pop up too happen
////if no then kill and restart the process
//
////find and click the checkout button
////wait for the next page to load
////if no then kill and restart the process
//
////basically just need to know that im in the next area correctly
