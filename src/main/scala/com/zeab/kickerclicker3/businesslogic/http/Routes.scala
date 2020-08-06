package com.zeab.kickerclicker3.businesslogic.http

import java.util.UUID

import akka.actor.Props
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import com.zeab.kickerclicker3.app.httpservice.{Marshallers, Unmarshallers}
import com.zeab.kickerclicker3.app.sqlconnection.MYSQLConnection
import com.zeab.kickerclicker3.businesslogic.http.models.{DeleteDropRequest, DeleteDropResponse, PostDropRequest, PostDropResponse}
import com.zeab.kickerclicker3.businesslogic.snrks.SnrksDropMonitor
import io.circe.generic.AutoDerivation

object Routes extends Directives with AutoDerivation with Marshallers with Unmarshallers {

  def routes: Route = drops

  def drops: Route =
    extractActorSystem { implicit system =>
      path("drops") {
        get {
          parameters("id".?) { (id: Option[String]) =>
            val drops: String =
              MYSQLConnection.selectDrops(id).map { drop =>
                s"""<tr>
                   |  <td>${drop.id}</td>
                   |  <td>${drop.name}</td>
                   |  <td>${drop.color}</td>
                   |  <td><a href=${drop.url}>  Link  </a></td>
                   |  <td>${drop.dateTime}</td>
                   |</tr>""".stripMargin
              }.mkString
            val response: String =
              s"""<!DOCTYPE html>
                 |<html>
                 |<head>
                 |<style>
                 |table, th, td {
                 |  border: 1px solid black;
                 |  border-collapse: collapse;
                 |}
                 |</style>
                 |</head>
                 |<body>
                 | <table style="width:100%">
                 |  <tr>
                 |    <th>id</th>
                 |    <th>name</th>
                 |    <th>color</th>
                 |    <th>url</th>
                 |    <th>datetime</th>
                 |  </tr>
                 |  $drops
                 |</table>
                 |</body>
                 |</html>""".stripMargin
            complete(response)
          }
        } ~
          post {
            decodeRequest {
              entity(as[PostDropRequest]) { drop: PostDropRequest =>
                val id: String = UUID.randomUUID().toString
                MYSQLConnection.insertDrop(id, drop.name, drop.color, drop.url, drop.dateTime, "1", "0")
                drop match {
                  case drop if drop.url.contains("www.nike.com") =>
                    system.actorOf(Props(classOf[SnrksDropMonitor], id, drop.url, drop.dateTime))
                  case drop => println(s"${drop.url} is not supported yet")
                }
                complete(StatusCodes.Created, PostDropResponse(id))
              }
            }
          } ~
          delete {
            decodeRequest {
              entity(as[DeleteDropRequest]) { drop: DeleteDropRequest =>
                MYSQLConnection.deleteDrop(drop.id)
                complete(StatusCodes.Accepted, DeleteDropResponse(drop.id))
              }
            }
          }
      }
    }

}
