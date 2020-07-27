package com.zeab.kickerclicker.httpservice

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.zeab.kickerclicker.snrks.{PostDropRequestBody, PostUser, PostUserResponse}
import com.zeab.kickerclicker.sqlconnection.{PostDropResponse, SQLConnection}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

object Routes {

  val drops: Route =
    extractActorSystem{implicit  system:ActorSystem =>
      path("drops"){
        get{
          parameters("id".?) { (id: Option[String]) =>
            complete(StatusCodes.OK, SQLConnection.selectDrops(id))
          }
        } ~
          post{
            decodeRequest {
              entity(as[PostDropRequestBody]) { req: PostDropRequestBody =>
                val dropId: String = UUID.randomUUID().toString
                SQLConnection.insertDrop(dropId, req.url, req.dateTime, req.monitorPeriod)
                complete(StatusCodes.Created, PostDropResponse(dropId))
              }
            }
          } ~
          delete{
            parameters("id") { (id: String) =>
              SQLConnection.deleteDrop(id)
              complete(StatusCodes.OK, "drop deleted")
            }
          }
      }
    }

  val route: Route = drops

}
