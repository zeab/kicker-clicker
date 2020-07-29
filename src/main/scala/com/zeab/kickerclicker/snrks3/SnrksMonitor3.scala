//package com.zeab.kickerclicker.snrks3
//
//import java.io.File
//import java.net.URL
//
//import akka.actor.Actor
//import com.zeab.kickerclicker.utilities.ThreadLocalRandom
//import org.apache.commons.io.FileUtils
//import org.openqa.selenium.firefox.FirefoxOptions
//import org.openqa.selenium.remote.RemoteWebDriver
//import org.openqa.selenium._
//
//import scala.collection.JavaConverters._
//import scala.concurrent.ExecutionContext
//import scala.concurrent.duration._
//import scala.util.{Failure, Success, Try}
//
//class SnrksMonitor3(host: String, port: Int, name: String) extends Actor{
//
//  //val sizesToLookFor: List[String] = List("7", "9", "9.5", "10", "10.5", "11", "11.5", "12")
//  val sizesToLookFor: List[String] = List("8")
//
//  val email = ""
//  val password = ""
//  val cv = ""
//
//  implicit val ec: ExecutionContext = context.system.dispatcher
//
//  val options: FirefoxOptions = new FirefoxOptions
//  //Set for headless mode
//  options.setHeadless(true)
//  //Set so we don't load images
//  options.addPreference("permissions.default.image", 2)
//  options.addPreference("dom.ipc.plugins.enabled.libflashplayer.so", "false")
//  //Set the zoom so we see the entire page
//  options.addPreference("layout.css.devPixelsPerPx", "0.5")
//  //options.addPreference("network.proxy.http", "localhost")
//  //options.addPreference("network.proxy.http_port", 7000)
//  //options.addPreference("network.proxy.type", 1)
//
//  val firefox: WebDriver = new RemoteWebDriver(new URL(s"http://$host:$port/wd/hub"), options)
//
//  val maleOrFemale = "F"
//  val size = "10.5"
//
//  def receive: Receive = processing()
//
//  def processing(retryCount: Int = 0): Receive = {
//    case OpenWebSite =>
//      firefox.manage().window().maximize()
//      println("opening website")
//      val url: String = s"https://www.nike.com/launch/t/$name"
//      firefox.get(url)
//      firefox.getTitle match {
//        case null =>
//          takeScreenshot(name, "error")
//          throw new Exception("the web title is null so i don't think we hit what we expected")
//        case _ =>
//          println("the site seems to be up so moving on to the next step")
//          takeScreenshot(name)
//          context.system.scheduler.scheduleOnce(5.second){self ! LookForBuyingContainer}
//      }
//    case LookForBuyingContainer =>
//      Try(firefox.findElement(By.xpath("//div[contains(@class, 'buying-tools-container')]"))) match {
//        case Failure(exception) =>
//          takeScreenshot(name)
//          println(exception.toString)
//          context.system.scheduler.scheduleOnce(1.second)(self ! LookForBuyingContainer)
//        case Success(buyingGrid: WebElement) =>
//          val buttonsFound: List[WebElement] =
//            buyingGrid.findElements(By.tagName("button")).asScala.toList
//          if (buttonsFound.isEmpty)
//            Try(buyingGrid.findElement(By.xpath(s"//div[contains(.,'Sold Out')]"))) match {
//              case Failure(exception) =>
//                println("was looking for the sold out button but could not find it")
//              case Success(_) =>
//                println("sold out :( shutting down")
//                firefox.close()
//                context.stop(self)
//            }
//          else if (buttonsFound.size == 1)
//            Try(buyingGrid.findElement(By.xpath(s"//button[contains(.,'Notify Me')]"))) match {
//              case Failure(exception) =>
//                println("we were looking for the notify button but did not find it... looking for  enter draw button")
//                context.system.scheduler.scheduleOnce(2.second)(self ! LookForBuyingContainer)
//              case Success(_) =>
//                println("notify button found sleeping for 2 seconds and trying again")
//                context.system.scheduler.scheduleOnce(2.second)(self ! LookForBuyingContainer)
//            }
//          else {
//            val sizesAvailable: List[(String, WebElement, Boolean)] =
//              buttonsFound.map(element => (element.getText, element, element.isEnabled)).filter(_._3 == true)
//            if (sizesAvailable.isEmpty) {
//              println("none of the sizes seem to be available to click? stopping")
//              firefox.close()
//              context.stop(self)
//            }
//            else {
//              val sizeToBuy: String = ThreadLocalRandom.getRandomItemFromCollection(sizesToLookFor)
//              val checkForUnisexButtons: List[(String, WebElement, Boolean)] = sizesAvailable.filter(_._1.contains('/'))
//              if (checkForUnisexButtons.isEmpty) {
//                //these are straight numbers
//                val possibleButton: Option[(String, WebElement, Boolean)] = sizesAvailable.find(_._1 == sizeToBuy)
//                possibleButton match {
//                  case Some(button: (String, WebElement, Boolean)) =>
//                    println(s"clicking on the size $sizeToBuy")
//                    button._2.click()
//                    context.system.scheduler.scheduleOnce(1.second)(self ! AddToCart)
//                  case None =>
//                    println("we cant find the size of the button were looking for...?? stopping cause idk whats up")
//                    firefox.close()
//                    context.stop(self)
//                }
//              }
//              else {
//                //need to filter out the m and w sizes
//                val possibleButton: Option[(String, WebElement, Boolean)] =
//                  sizesAvailable.find(_._1.contains(s"M $sizeToBuy"))
//                possibleButton match {
//                  case Some(button: (String, WebElement, Boolean)) =>
//                    println(s"found the size button clicking $sizeToBuy")
//                    button._2.click()
//                    context.system.scheduler.scheduleOnce(1.second)(self ! AddToCart)
//                  case None =>
//                    println("could not find button")
//                }
//              }
//            }
//            takeScreenshot(name)
//          }
//      }
//    case AddToCart =>
//      buttonClick(
//        s"//button[contains(.,'Enter Drawing')]",
//        AddToCart,
//        EnterEmailAddress,
//        "unable to find enter drawing",
//        "clicking the enter drawing button"
//      )
//      takeScreenshot(name)
//    case GoToCart =>
//      firefox.get("https://www.nike.com/us/en/cart")
//      firefox.getTitle match {
//        case null =>
//          takeScreenshot(name, "error")
//          throw new Exception("the web title is null so i don't think we hit what we expected")
//        case title: String =>
//          if (title.contains("Cart")) {
//            println("looks like were in the cart continuing")
//            context.system.scheduler.scheduleOnce(1.second){self ! Checkout}
//          }
//          else {
//            takeScreenshot(name)
//            println("not on the cart yet sleeping for 5 seconds and retrying")
//            context.system.scheduler.scheduleOnce(5.second){self ! GoToCart}
//          }
//      }
//    case Checkout =>
//      buttonClick(
//        "//button[contains(.,'Checkout')]",
//        Checkout,
//        EnterEmailAddress,
//        "unable to find checkout button",
//        "clicking the checkout button"
//      )
//      takeScreenshot(name)
//    case EnterEmailAddress =>
//      println("enter email phase")
//      Try(firefox.findElement(By.xpath("//input[@placeholder='Email address']"))) match {
//        case Failure(exception) =>
//          println("cant find the email address waiting and looking again")
//          context.system.scheduler.scheduleOnce(3.second)(self ! EnterEmailAddress)
//        case Success(element) =>
//          if (element.isDisplayed) {
//            println("sending email address keys")
//            element.sendKeys(email)
//            context.system.scheduler.scheduleOnce(3.second)(self ! EnterPassword)
//          }
//          else {
//            println("email address is not displayed")
//            context.system.scheduler.scheduleOnce(3.second)(self ! EnterEmailAddress)
//          }
//      }
//      takeScreenshot(name)
//    case EnterPassword =>
//      println("enter password")
//      Try(firefox.findElement(By.xpath("//input[@placeholder='Password']"))) match {
//        case Failure(exception) =>
//          println("cant find the password waiting and looking again")
//          context.system.scheduler.scheduleOnce(3.second)(self ! EnterPassword)
//        case Success(element) =>
//          if (element.isDisplayed) {
//            element.sendKeys(password)
//            context.system.scheduler.scheduleOnce(3.second)(self ! Login)
//          }
//          else context.system.scheduler.scheduleOnce(3.second)(self ! EnterPassword)
//      }
//      takeScreenshot(name)
//    case Login =>
//      Try(firefox.findElement(By.xpath("//*[@value='MEMBER CHECKOUT']"))) match {
//        case Failure(exception) =>
//          println("cant find member checkout")
//          context.system.scheduler.scheduleOnce(1.second)(self ! Login)
//        case Success(button) =>
//          println("member checkout clicking")
//          button.click()
//          context.system.scheduler.scheduleOnce(1.second)(self ! EnterCV)
//      }
//      takeScreenshot(name)
//    case EnterCV =>
//      println("entering cv")
//      Try(firefox.findElements(By.tagName("iframe"))) match {
//        case Failure(exception) =>
//          println("cant find any iframes")
//          context.system.scheduler.scheduleOnce(ThreadLocalRandom.getRandomInt(1000, 500).milli)(self ! EnterCV)
//        case Success(iFrames) =>
//          val foundOrNot =
//            iFrames.asScala.toList.map { element =>
//              println("trying to switch to element")
//              Try(firefox.switchTo().frame(element))
//              println("trying to find cv number input")
//              Try(firefox.findElement(By.xpath("//*[@id='cvNumber']"))) match {
//                case Failure(exception) =>
//                  println("cant find cv")
//                  Try(firefox.switchTo().defaultContent())
//                  false
//                case Success(hy) =>
//                  println("found cv")
//                  hy.sendKeys(cv)
//                  Try(firefox.switchTo().defaultContent())
//                  true
//              }
//            }.filter(_ == true)
//          if (foundOrNot.nonEmpty) context.system.scheduler.scheduleOnce(4000.milli)(self ! SubmitOrder)
//          else context.system.scheduler.scheduleOnce(1000.milli)(self ! EnterCV)
//      }
//      takeScreenshot(name)
//    case SubmitOrder =>
//      Try(firefox.findElement(By.xpath("//button[contains(.,'Place Order')]"))) match {
//        case Failure(exception) =>
//          context.system.scheduler.scheduleOnce(4.second)(self ! SubmitOrder)
//          println("unable to find submit order button")
//        case Success(button) =>
//          println("clicking the submit order button")
//          button.click()
//          context.system.scheduler.scheduleOnce(ThreadLocalRandom.getRandomInt(1000, 500).milli)(self ! End)
//      }
//      takeScreenshot(name)
//    case EnterDrawing =>
//      println("entering cv for drawing")
//      Try(firefox.findElements(By.tagName("iframe"))) match {
//        case Failure(exception) =>
//          println("cant find any iframes")
//          context.system.scheduler.scheduleOnce(ThreadLocalRandom.getRandomInt(1000, 500).milli)(self ! EnterCV)
//        case Success(iFrames) =>
//          val foundOrNot =
//            iFrames.asScala.toList.map { element =>
//              println("trying to switch to element")
//              Try(firefox.switchTo().frame(element))
//              println("trying to find cv number input")
//              Try(firefox.findElement(By.xpath("//*[@id='cvNumber']"))) match {
//                case Failure(exception) =>
//                  println("cant find cv")
//                  Try(firefox.switchTo().defaultContent())
//                  false
//                case Success(hy) =>
//                  println("found cv")
//                  hy.sendKeys(cv)
//                  Try(firefox.switchTo().defaultContent())
//                  true
//              }
//            }.filter(_ == true)
//          if (foundOrNot.nonEmpty) context.system.scheduler.scheduleOnce(4000.milli)(self ! SubmitDrawing)
//          else context.system.scheduler.scheduleOnce(1000.milli)(self ! EnterDrawing)
//      }
//    case SubmitDrawing =>
//      println()
//      takeScreenshot(name)
//      self ! SubmitOrder
//    case End =>
//      Thread.sleep(5000)
//      takeScreenshot(name)
//      println("this is the end")
//  }
//
//  override def preStart(): Unit = {
//    self ! OpenWebSite
//  }
//
//  def takeScreenshot(name: String, suffix: String = "live"): Unit ={
//    val file: File = firefox.asInstanceOf[TakesScreenshot].getScreenshotAs(OutputType.FILE)
//    FileUtils.copyFile(file, new File(s"C:/selenium/${System.currentTimeMillis()}-$name-$suffix.png"))
//  }
//
//  case object SubmitDrawing
//
//  case object EnterDrawing
//
//  case object SubmitOrder
//
//  case object EnterCV
//
//  case object Login
//
//  case object EnterPassword
//
//  case object EnterEmailAddress
//
//  case object Checkout
//
//  case object GoToCart
//
//  case object AddToCart
//
//  case object LookForBuyingContainer
//
//  case object OpenWebSite
//
//  case object LookForSize
//
//  case object End
//
//}
//
