//package com.zeab.kickerclicker.monitor
//
//import java.io.File
//
//import org.apache.commons.io.FileUtils
//import org.openqa.selenium.{By, OutputType, TakesScreenshot, WebDriver, WebElement}
//
//import scala.util.Try
//
//trait MonitorStuff {
//
//  def takeScreenshot(id: String, name: String, suffix: String = "live")(implicit webDriver: WebDriver): Unit = {
//    val file: File = webDriver.asInstanceOf[TakesScreenshot].getScreenshotAs(OutputType.FILE)
//    FileUtils.copyFile(file, new File(s"/selenium/$id/${System.currentTimeMillis()}$name-$suffix.png"))
//  }
//
//  import org.openqa.selenium.support.ui.ExpectedConditions
//  import org.openqa.selenium.support.ui.WebDriverWait
//
//  def waitUntilPageLoaded(timeoutSeconds: Int, locator: By)(implicit webDriver: WebDriver): Try[WebElement] =
//    Try(new WebDriverWait(webDriver, timeoutSeconds).until(ExpectedConditions.presenceOfElementLocated(locator)))
//
////  def buttonClick[A, B](
////                         searchString: String,
////                         retry: A,
////                         continue: B,
////                         failureMessage: String,
////                         successMessage: String,
////                         failureWait: Int = 4,
////                         successWaitMax: Int = 2000,
////                         successWaitMin: Int = 1000
////                       )(implicit webDriver: WebDriver, context: ActorContext): Unit =
////    Try(webDriver.findElement(By.xpath(searchString))) match {
////      case Failure(exception: Throwable) =>
////        context.system.log.debug(exception.toString)
////        println(failureMessage)
////        context.system.scheduler.scheduleOnce(failureWait.second)(self ! retry)
////      case Success(button: WebElement) =>
////        println(successMessage)
////        Try(button.click()) match {
////          case Failure(exception: Throwable) =>
////            context.system.log.debug(exception.toString)
////            println(failureMessage)
////            context.system.scheduler.scheduleOnce(failureWait.second)(self ! retry)
////          case Success(_) =>
////            context.system.scheduler.scheduleOnce(ThreadLocalRandom.getRandomInt(successWaitMax, successWaitMin).milli)(self ! continue)
////        }
////    }
////
////  def input[A, B](
////                   input: String,
////                   searchString: String,
////                   retry: A,
////                   continue: B,
////                   failureMessage: String,
////                   successMessage: String,
////                   failureWait: Int = 4,
////                   successWaitMax: Int = 2000,
////                   successWaitMin: Int = 1000
////                 ): Unit =
////    Try(firefox.findElement(By.xpath(searchString))) match {
////      case Failure(exception: Throwable) =>
////        context.system.log.error(exception.toString)
////        println(failureMessage)
////        context.system.scheduler.scheduleOnce(failureWait.second)(self ! retry)
////      case Success(inputArea: WebElement) =>
////        println(successMessage)
////        Try(inputArea.sendKeys(input)) match {
////          case Failure(exception: Throwable) =>
////            context.system.log.error(exception.toString)
////            println(failureMessage)
////            context.system.scheduler.scheduleOnce(failureWait.second)(self ! retry)
////          case Success(_) =>
////            context.system.scheduler.scheduleOnce(ThreadLocalRandom.getRandomInt(successWaitMax, successWaitMin).milli)(self ! continue)
////        }
////    }
//
//}
