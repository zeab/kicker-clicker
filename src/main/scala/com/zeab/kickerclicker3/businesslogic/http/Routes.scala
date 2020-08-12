package com.zeab.kickerclicker3.businesslogic.http

import java.time.ZonedDateTime
import java.util.{Date, UUID}

import akka.actor.Props
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import com.zeab.kickerclicker3.app.httpservice.{Marshallers, Unmarshallers}
import com.zeab.kickerclicker3.app.sqlconnection.MYSQLConnection
import com.zeab.kickerclicker3.app.sqlconnection.tables.DropsTable
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
              MYSQLConnection.selectDrops(id).sortBy(_.dateTime).map { drop: DropsTable =>
                s"""<a class="card" href="${drop.url}">
                   |  <img src="${drop.imageUrl}" alt="card image">
                   |  <h2>${if (drop.name == "") "undefined" else drop.name}</h2>
                   |  <p>${if (drop.color == "") "undefined" else drop.color}</p>
                   |  <p>${new Date(drop.dateTime)}</p>
                   |</a>""".stripMargin
              }.mkString

            val response: String =
              s"""<!DOCTYPE html>
                 |<html>
                 |<head>
                 |<meta name="viewport" content="width=device-width, initial-scale=1">
                 |<style>
                 |img{
                 |  width:100%;
                 |  max-width:200px;
                 |}
                 |
                 |.wrapper {
                 |  display: grid;
                 |  grid-template-columns: repeat(3, 1fr);
                 |  grid-gap: 10px;
                 |  grid-auto-rows: minmax(100px, auto);
                 |}
                 |
                 |@media screen and (max-width: 600px) {
                 |  .wrapper {
                 |    grid-template-columns: repeat(1, 1fr);
                 |  }
                 |}
                 |
                 |.card {
                 |  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2);
                 |  padding: 16px;
                 |  text-align: center;
                 |
                 |}
                 |</style>
                 |</head>
                 |<body>
                 |<div class="wrapper">
                 |$drops
                 |</div>
                 |</body>
                 |</html>""".stripMargin
            complete(response)
          }
        } ~
          post {
            decodeRequest {
              entity(as[PostDropRequest]) { drop: PostDropRequest =>
                val id: String = UUID.randomUUID().toString
                val dropDateTime: Long = ZonedDateTime.parse(drop.dateTime).toInstant.toEpochMilli
                MYSQLConnection.insertDrop(id, drop.name, drop.color, drop.url, drop.imageUrl, dropDateTime, drop.isWanted)
                drop match {
                  case drop if drop.url.contains("www.nike.com") =>
                    system.actorOf(Props(classOf[SnrksDropMonitor], id, drop.url, dropDateTime))
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
