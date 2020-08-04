//package com.zeab.kickerclicker.buyer
//
//import java.time.ZonedDateTime
//import java.util.concurrent.TimeUnit
//import java.util.{Date, Timer, TimerTask}
//
//import akka.actor.{ActorRef, PoisonPill}
//import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
//import org.openqa.selenium.{By, WebDriver, WebElement}
//
//import scala.util.Try
//
//trait Buyer {
//
//  def waitUntilPageLoaded(timeoutSeconds: Int, locator: By)(implicit webDriver: WebDriver): Try[WebElement] =
//    Try(new WebDriverWait(webDriver, timeoutSeconds).until(ExpectedConditions.presenceOfElementLocated(locator)))
//
//  def scheduleStart(dropDateTime: ZonedDateTime, self: ActorRef): Unit = {
//    val now: ZonedDateTime = ZonedDateTime.now()
//    if (now.isBefore(dropDateTime.plusMinutes(10))) {
//      val timer: Timer = new Timer()
//      val task: TimerTask = new TimerTask() { override def run(): Unit = { self ! Init } }
//      timer.schedule(task, Date.from(dropDateTime.toInstant), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS))
//    }
//    else self ! PoisonPill
//  }
//
//  case object Init
//
//  case object GetWebSite
//
//}
