package com.lynbrookrobotics.funkydashboard

import me.shadaj.slinky.core.facade.ReactElement
import play.api.libs.json.Json

import scala.collection.immutable.Queue

object Dataset {
  def extract(definition: DatasetDefinition, sendData: String => Unit): Queue[TimedValue[String]] => ReactElement = {
    definition match {
      case DatasetDefinition(_, "time-series-numeric") =>
        values => TimeSeriesNumeric(
          values.map(v => TimedValue(v.time, Json.parse(v.value).as[Double]))
        )

      case DatasetDefinition(_, "time-multiple-dataset") =>
        values => MultipleTimeSeriesNumeric(
          values.map(v => TimedValue(v.time, Json.parse(v.value).as[List[Double]]))
        )

      case DatasetDefinition(_, "table") =>
        values => TableDataset(
          values.map(v => TimedValue(v.time, Json.parse(v.value).as[List[TablePair]]))
        )

      case DatasetDefinition(_, "time-snapshot") =>
        values => TimeSnapshotNumeric(
          values.map(v => TimedValue(v.time, Json.parse(v.value).as[List[Double]].headOption))
        )

      case DatasetDefinition(_, "image-stream") =>
        values => ImageStream(
          values.map(v => Json.parse(v.value).as[String])
        )

      case DatasetDefinition(_, "time-text") =>
        values => TimeText(
          values.map(v => TimedValue(v.time, Json.parse(v.value).as[String]))
        )

      case DatasetDefinition(_, "json-editor") => values =>
        JsonEditorDataset(
          values.map(v => TimedValue(v.time, Json.parse(v.value).as[String])),
          sendData
        )
    }
  }
}
