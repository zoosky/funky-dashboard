package com.lynbrookrobotics.funkydashboard

import akka.stream.scaladsl.{Sink, Source}
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws.{TextMessage, UpgradeToWebSocket}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.collection.mutable
import scala.concurrent.duration._
import upickle.default._

import scala.language.postfixOps

class FunkyDashboard {
  private var datasetGroups = Map[String, DatasetGroup]()

  private val source = Source.tick(0 millis, 125 millis, ()).map { _ =>
    val time = System.currentTimeMillis()
    val toSend = (time, datasetGroups.map(t => t._1 -> t._2.currentValue))
    TextMessage(write(toSend))
  }

  private val sink = Sink.ignore

  val route: Route = get {
    pathSingleSlash {
      getFromResource("META-INF/resources/index.html")
    } ~ path("datasets.json") {
      complete(HttpResponse(
        entity = HttpEntity(
          ContentTypes.`application/json`,
          write(datasetGroups.values.map(_.properties))
        )
      ))
    } ~ path("datastream") {
      optionalHeaderValueByType[UpgradeToWebSocket](()) {
        case Some(header) =>
          complete(header.handleMessagesWithSinkSource(sink, source))
        case None =>
          complete(HttpResponse(
            status = StatusCodes.BadRequest,
            entity = "Expected websocket request"
          ))
      }
    } ~ pathPrefix("") {
      encodeResponse(getFromResourceDirectory("META-INF/resources"))
    }
  }

  def datasetGroup(key: String) =
    datasetGroups.getOrElse(key, {
      val group = new DatasetGroup(key)
      datasetGroups = datasetGroups + (key -> group)
      group
    })
}
