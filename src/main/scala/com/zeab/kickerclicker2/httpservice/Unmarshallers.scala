//package com.zeab.kickerclicker2.httpservice
//
////Imports
////import zeab.aenea.XmlDeserialize
////Akka
//import akka.http.scaladsl.model.MediaTypes.{`application/json`, `application/xml`}
//import akka.http.scaladsl.model.{ContentTypeRange, MediaType}
//import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
////Circe
//import io.circe.Decoder
//import io.circe.parser.decode
////Scala
//import scala.reflect.runtime.universe._
//
//trait Unmarshallers {
//
//  def unmarshallerContentTypes: Seq[ContentTypeRange] =
//    mediaTypes.map(ContentTypeRange.apply)
//
//  def mediaTypes: Seq[MediaType.WithOpenCharset] =
//    List(`application/xml`)
//
//  def jsonUnmarshaller[A: Decoder]: FromEntityUnmarshaller[A] =
//    Unmarshaller
//      .stringUnmarshaller
//      .forContentTypes(`application/json`)
//      .map{str: String =>
//        decode[A](str) match {
//          case Right(encodedValue) => encodedValue
//          case Left(exception: Exception) => throw exception
//        }
//      }
//
////  def xmlUnmarshaller[A](implicit typeTag: TypeTag[A]): FromEntityUnmarshaller[A] =
////    Unmarshaller
////      .stringUnmarshaller
////      .forContentTypes(unmarshallerContentTypes: _*)
////      .map{str =>
////        XmlDeserialize.xmlDeserialize[A](str) match {
////          case Right(value) => value
////          case Left(ex) => throw ex
////        }
////      }
//
//  implicit final def unmarshaller[A: Decoder](implicit typeTag: TypeTag[A]): FromEntityUnmarshaller[A] =
//    Unmarshaller.firstOf(jsonUnmarshaller)
//
//}
