package com.lynbrookrobotics.funkydashboard

import org.scalajs.dom.ext.Ajax
import scala.scalajs.js
import scalajs.concurrent.JSExecutionContext.Implicits.queue

import scala.scalajs.js.{JSON, JSApp}
import org.scalajs.dom._
import org.scalajs.jquery._

object DashboardClient extends JSApp {
  val groupChooser = jQuery("#group-chooser")
  val sideDrawer = jQuery(".mdl-layout__drawer")

  val websocketProtocol = if (window.location.protocol == "https") {
    "wss"
  } else {
    "ws"
  }

  def initWebsocket(datasetsTree: Map[String, Map[String, Dataset[Nothing]]]): Unit = {
    val datastream = new WebSocket(s"$websocketProtocol://${window.location.host}/datastream")
    datastream.onmessage = (e: MessageEvent) => {
      val currentDataDictionary = JSON.parse(e.data.toString).asInstanceOf[js.Dictionary[js.Any]]
      currentDataDictionary.foreach { case (groupName, datasetsObject) =>
        val datasets = datasetsObject.asInstanceOf[js.Dictionary[js.Any]]
        datasets.foreach { case (datasetName, currentValueObject) =>
          datasetsTree(groupName)(datasetName).update(currentValueObject)
        }
      }
    }
  }

  def main(): Unit = {
    Ajax.get("/datasets.json").foreach { result =>
      val datasetGroups = JSON.parse(result.responseText).asInstanceOf[js.Array[DatasetGroup]]
      val tree = datasetGroups.toList.zipWithIndex.map { case (datasetGroup, index) =>
        val groupView = document.createElement("div").asInstanceOf[html.Div]
        groupView.className = "mdl-grid group-view"
        val datasets = datasetGroup.datasets.map { datasetDefinition =>
          val dataset = Dataset.extract(datasetDefinition)
          groupView.appendChild(dataset.card)
          datasetDefinition.name -> dataset
        }.toMap

        groupView.style.display = if (index == 0) "flex" else "none"
        document.getElementById("groups-container").appendChild(groupView)

        val sidebarLink = document.createElement("a").asInstanceOf[html.Anchor]
        sidebarLink.className = "mdl-navigation__link"
        sidebarLink.innerHTML = datasetGroup.name
        sidebarLink.href = ""
        sidebarLink.style.backgroundColor = if (index == 0) "#00ACC1" else "transparent"
        sidebarLink.onclick = (e: MouseEvent) => {
          jQuery(".group-view").get().foreach { elem =>
            elem.asInstanceOf[html.Div].style.display =
              if (groupView == elem) "flex" else "none"
          }

          jQuery(".mdl-navigation__link").get().foreach { elem =>
            elem.asInstanceOf[html.Anchor].style.backgroundColor =
              if (sidebarLink == elem) "#00ACC1" else "transparent"
          }
//          println(s"${element.name} clicked!")
          sideDrawer.removeClass("is-visible")
          e.preventDefault()
        }

        groupChooser.append(sidebarLink)

        datasetGroup.name -> datasets
      }.toMap

      initWebsocket(tree)
    }
  }
}
