ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.2.2"

//addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")

lazy val root = (project in file("."))
  .settings(
    name := "zuikaku",
    idePackagePrefix := Some("rip.deadcode.zuikaku"),
    libraryDependencies ++= Seq(
//        "dev.zio" %% "zio" % "2.0.6",
//        "dev.zio" %% "zio-logging-slf4j" % "2.1.8",
      "org.typelevel" %% "cats-core" % "2.9.0",
      "org.typelevel" %% "cats-effect" % "3.4.5",
      "com.google.guava" % "guava" % "31.1-jre",
      "commons-cli" % "commons-cli" % "1.5.0",
      "io.circe" %% "circe-yaml-v12" % "0.14.3-RC3",
      "io.circe" %% "circe-core" % "0.14.3",
      "io.circe" %% "circe-parser" % "0.14.3",
      "io.circe" %% "circe-generic" % "0.14.3",
      //      "io.circe" %% "circe-generic-extras" % "0.14.3",
//      "io.circe" %% "circe-config" % "0.14.3",
      "com.vladsch.flexmark" % "flexmark-all" % "0.64.0",
      "org.slf4j" % "jul-to-slf4j" % "2.0.5",
      "ch.qos.logback" % "logback-classic" % "1.4.5",
      "org.scalatest" %% "scalatest" % "3.2.15" % "test"
    )
  )
