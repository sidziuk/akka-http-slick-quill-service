lazy val commonSettings = Seq(
  organization := "com.sidziuk",
  scalaVersion := "2.12.12",
  scalacOptions := List("-encoding", "utf8", "-Xfatal-warnings", "-deprecation", "-unchecked"),
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-library" % "2.13.6",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
    "junit" % "junit" % "4.13.2" % Test,
    "org.scalatest" %% "scalatest" % "3.2.9" % Test
  )
)

lazy val application = (project in file("app"))
  .settings(
    commonSettings,
    name := "application",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.9",
      "com.typesafe.akka" %% "akka-actor" % "2.6.17",
      "com.typesafe.akka" %% "akka-stream" % "2.6.17",
      "com.typesafe.akka" %% "akka-http" % "10.2.6",
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.6",
      "io.spray" %% "spray-json" % "1.3.6",
      "org.mariadb.jdbc" % "mariadb-java-client" % "3.0.7",
      "com.typesafe.play" %% "play-slick" % "5.1.0",
      "io.getquill" %% "quill-jasync-mysql" % "4.6.0"
    )
  )

lazy val root = (project in file("."))
  .settings(
    name := "SlickAndQuillExample",
    Compile / mainClass := Some("com.sidziuk.Runner"),
    commonSettings
  )
  .dependsOn(application)
  .aggregate(
    application,
  )
