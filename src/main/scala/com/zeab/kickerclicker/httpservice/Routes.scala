//package com.zeab.kickerclicker.httpservice
//
//import java.time.ZonedDateTime
//import java.util.UUID
//
//import akka.actor.ActorSystem
//import akka.http.scaladsl.model.StatusCodes
//import akka.http.scaladsl.server.Directives._
//import akka.http.scaladsl.server.Route
//import com.zeab.kickerclicker.monitor.MonitorFactory
//import com.zeab.kickerclicker.selenium.PostDropResponse
//import com.zeab.kickerclicker2.sqlconnection.SQLConnection
//import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
//import io.circe.generic.auto._
//
//import scala.util.{Failure, Success, Try}
//
//object Routes {
//
//  val drops: Route =
//    extractActorSystem { implicit system: ActorSystem =>
//      path("drops") {
//          post {
//            decodeRequest {
//              entity(as[PostDropRequestBody]) { req: PostDropRequestBody =>
//                val dropId: String = UUID.randomUUID().toString
//                SQLConnection.selectDrops(url = Some(req.url)) match {
//                  case Nil =>
//                    Try(ZonedDateTime.parse(req.dateTime)) match {
//                      case Failure(_) =>
//                        complete(StatusCodes.InternalServerError, "date time is not valid format")
//                      case Success(_) =>
//                        Try(ZonedDateTime.parse(req.dateTime)) match {
//                          case Failure(_) => complete(StatusCodes.InternalServerError, s"datetime is not in the right format")
//                          case Success(goodDateTime: ZonedDateTime) =>
//                            MonitorFactory.startMonitor(dropId, req.url, goodDateTime)
//                            SQLConnection.insertDrop(dropId, "" , "", req.url, req.dateTime, "", req.monitorPeriod)
//                            complete(StatusCodes.Created, PostDropResponse(dropId))
//                        }
//                    }
//                  case ::(selectedDrop, _) =>
//                    complete(StatusCodes.InternalServerError, s"already created a drop for that url: ${selectedDrop.id}")
//                }
//              }
//            }
//          } ~
//          delete {
//            parameters("id") { (id: String) =>
//              SQLConnection.deleteDrop(id)
//              complete(StatusCodes.OK, "drop deleted")
//            }
//          }
//      }
//    }
//
//  val route: Route = drops ~ GetDrops.drops
//
//}
