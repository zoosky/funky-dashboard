enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)

scalaVersion := "2.11.8"

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.0"

libraryDependencies += "com.github.japgolly.scalajs-react" %%% "core" % "0.11.0"

libraryDependencies += "com.payalabs" %%% "scalajs-react-mdl" % "0.2.0-SNAPSHOT"

npmDependencies in Compile += "react" -> "15.0.1"
npmDependencies in Compile += "react-dom" -> "15.0.1"
npmDependencies in Compile += "moment" -> "2.16.0"
npmDependencies in Compile += "chart.js" -> "2.3.0"
