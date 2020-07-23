package com.zeab.kickerclicker.httpservice

import java.io.{BufferedWriter, File, FileWriter}
import java.time.{ZoneId, ZonedDateTime}
import java.util.concurrent.TimeUnit
import java.util.{Calendar, Date, Timer, TimerTask, UUID}

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.zeab.kickerclicker.snrks.{DropResponse, PostDropRequestBody, PostUser, PostUserResponse, SnrksMonitor, UserDatabase}
import com.zeab.kickerclicker.snrks2.SnrksMonitor2
import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.syntax._

object Routes {

  val users: Route =
    path("users"){
      parameters("id".?) { (id: Option[String]) =>
        id match {
          case Some(userId: String) =>
            //get the specific user
            complete(StatusCodes.Created, "")
          case None =>
            //get all the users
            complete(StatusCodes.Created, "")
        }
      } ~
        post{
          decodeRequest {
            entity(as[PostUser]) { req =>
              val userId: String = UUID.randomUUID().toString
              complete(StatusCodes.Created, PostUserResponse(userId))
            }
          }
        }
    }

  val drops: Route =
    extractActorSystem{implicit  system:ActorSystem =>
      path("drops"){
        get{
          parameters("id".?) { (id: Option[String]) =>
            id match {
              case Some(dropId: String) =>
                //get the specific drop
                complete(StatusCodes.Created, "")
              case None =>
                //get all the drops
                complete(StatusCodes.Created, "")
            }
          }
        } ~
          post{
            decodeRequest {
              entity(as[PostDropRequestBody]) { req: PostDropRequestBody =>
                val dropId: String = UUID.randomUUID().toString
                system.actorOf(Props(classOf[SnrksMonitor2], req.shoeName, req.shoeSize, req.isMale, req.email, req.password, req.cv, req.dateTime), dropId)
                complete(StatusCodes.Created, PostUserResponse(dropId))
              }
            }
          }
      }
    }

  val route: Route = users ~ drops

}
