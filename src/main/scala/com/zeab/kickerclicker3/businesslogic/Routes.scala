package com.zeab.kickerclicker3.businesslogic

import akka.http.scaladsl.server.{Directives, Route}
import com.zeab.kickerclicker3.app.httpservice.{Marshallers, Unmarshallers}

object Routes extends Directives with Marshallers with Unmarshallers {

  def routes: Route = drops

  def drops: Route =
    path("drops") {
      get {
        parameters("id".?) { (id: Option[String]) =>
          complete()
        }
      } ~
        post {
          decodeRequest {
            entity(as[String]) { req: String =>
              complete()
            }
          }
        } ~
        delete {
          decodeRequest {
            entity(as[String]) { req: String =>
              complete()
            }
          }
        }
    }

}
