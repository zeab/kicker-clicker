//package com.zeab.kickerclicker2.httpservice
//
//import akka.http.scaladsl.server.{Directives, Route}
//import com.zeab.kickerclicker2.sqlconnection.SQLConnection
//
//object Routes extends Directives with Marshallers with Unmarshallers {
//
//  val businessLogic: Route =
//    path("drops"){
//      get {
//        parameters("id".?) { (id: Option[String]) =>
//          val drops: String =
//            SQLConnection.selectDrops(id).map { drop =>
//              s"""<tr>
//                 |  <td>${drop.id}</td>
//                 |  <td>${drop.name}</td>
//                 |  <td>${drop.color}</td>
//                 |  <td><a href=${drop.url}>  Link  </a></td>
//                 |  <td>${drop.dateTime}</td>
//                 |</tr>""".stripMargin
//            }.mkString
//          val response: String =
//            s"""<!DOCTYPE html>
//               |<html>
//               |<head>
//               |<style>
//               |table, th, td {
//               |  border: 1px solid black;
//               |  border-collapse: collapse;
//               |}
//               |</style>
//               |</head>
//               |<body>
//               | <table style="width:100%">
//               |  <tr>
//               |    <th>id</th>
//               |    <th>name</th>
//               |    <th>color</th>
//               |    <th>url</th>
//               |    <th>datetime</th>
//               |  </tr>
//               |  $drops
//               |</table>
//               |</body>
//               |</html>""".stripMargin
//          complete(response)
//        }
//      }
//    }
//
//}
