//package com.zeab.kickerclicker.httpservice
//
//import akka.actor.ActorSystem
//import akka.http.scaladsl.model._
//import akka.http.scaladsl.server.Directives._
//import akka.http.scaladsl.server.Route
//import com.zeab.kickerclicker2.sqlconnection.SQLConnection
//
//object GetDrops {
//
//  val drops: Route =
//    extractActorSystem { implicit system: ActorSystem =>
//      path("drops") {
//        get {
//          parameters("id".?) { (id: Option[String]) =>
//
//            val drops =
//            SQLConnection.selectDrops(id).map{drop =>
//              s"""<tr>
//                 |  <td>${drop.id}</td>
//                 |  <td>${drop.name}</td>
//                 |  <td>${drop.color}</td>
//                 |  <td>${drop.url}</td>
//                 |  <td>${drop.dateTime}</td>
//                 |</tr>""".stripMargin
//            }.mkString
//
//            val response =
//              s"""<!DOCTYPE html>
//                |<html>
//                |<head>
//                |<style>
//                |table, th, td {
//                |  border: 1px solid black;
//                |  border-collapse: collapse;
//                |}
//                |</style>
//                |</head>
//                |<body>
//                | <table style="width:100%">
//                |  <tr>
//                |    <th>id</th>
//                |    <th>name</th>
//                |    <th>color</th>
//                |    <th>url</th>
//                |    <th>datetime</th>
//                |  </tr>
//                |  $drops
//                |</table>
//                |</body>
//                |</html>""".stripMargin
//
//            val x = HttpResponse(entity = HttpEntity(ContentType(MediaTypes.`text/html`, HttpCharsets.`UTF-8`), response))
//            complete(x)
//          }
//        }
//      }
//    }
//
//}
