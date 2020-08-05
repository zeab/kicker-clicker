package com.zeab.kickerclicker3.app.httpservice

//Imports
//import zeab.aenea.XmlSerialize
//Akka
import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model._
//Circe
import io.circe.Encoder
import io.circe.syntax._

trait Marshallers {

//  def xmlMarshaller[A: Encoder]: ToEntityMarshaller[A] =
//    Marshaller.withFixedContentType(ContentType(MediaTypes.`application/xml`, HttpCharsets.`UTF-8`)) { body =>
//      XmlSerialize.xmlSerialize[String](body) match {
//        case Right(xml) =>
//          HttpEntity(contentType = ContentType(MediaTypes.`application/xml`, HttpCharsets.`UTF-8`), xml)
//        case Left(_) =>
//          HttpEntity(contentType = ContentType(MediaTypes.`application/xml`, HttpCharsets.`UTF-8`), "error will robinson!")
//      }
//    }

  def jsonMarshaller[A: Encoder]: ToEntityMarshaller[A] =
    Marshaller.withFixedContentType(ContentTypes.`application/json`) { body =>
      HttpEntity(ContentTypes.`application/json`, body.asJson.noSpaces)
    }

  def textMarshaller[A]: ToEntityMarshaller[A] =
    Marshaller.withFixedContentType(ContentTypes.`text/plain(UTF-8)`) { body =>
      HttpEntity(ContentTypes.`text/plain(UTF-8)`, body.toString)
    }

  def htmlMarshaller[A]: ToEntityMarshaller[A] =
    Marshaller.withFixedContentType(ContentTypes.`text/html(UTF-8)`) { body =>
      HttpEntity(ContentTypes.`text/html(UTF-8)`, body.toString)
    }

  implicit final def marshaller[A: Encoder]: ToEntityMarshaller[A] =
    Marshaller.oneOf(jsonMarshaller, textMarshaller, htmlMarshaller)

}