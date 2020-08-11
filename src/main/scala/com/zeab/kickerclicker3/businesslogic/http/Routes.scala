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
                val x = """https://secure-images.nike.com/is/image/DotCom/CI1474_001_A_PREM?$SNKRS_COVER_WD$&amp;align=0,1"""
                s"""<a class="card" href="${drop.url}">
                   |  <img src="$x" alt="card image">
                   |  <h2>${if (drop.name == "") "undefined" else drop.name}</h2>
                   |  <p>${if (drop.color == "") "undefined" else drop.color}</p>
                   |  <p>${new Date(drop.dateTime)}</p>
                   |</a>""".stripMargin
              }.mkString

            "https://placeholdit.imgix.net/~text?txtsize=33&txt=318%C3%97180&w=318&h=180"

            val response: String =
              s"""<!DOCTYPE html>
                 |<html>
                 |<head>
                 |<meta name="viewport" content="width=device-width, initial-scale=1">
                 |<style>
                 |img{
                 |    width:100%;
                 |    max-width:200px;
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
                 |  background-color: #f1f1f1;
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
                MYSQLConnection.insertDrop(id, drop.name, drop.color, drop.url, dropDateTime, isWanted = true)
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
