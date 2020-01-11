name := """play-scala-opinionated-backend-seed"""

version := "1.0.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.10"

libraryDependencies += guice

libraryDependencies += "org.postgresql" % "postgresql" % "42.2.9"

libraryDependencies += "com.typesafe.play" %% "play-slick" % "4.0.2"
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "4.0.2"

libraryDependencies += "com.github.tminglei" %% "slick-pg" % "0.17.3"
libraryDependencies += "com.github.tminglei" %% "slick-pg_play-json" % "0.17.3"

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % Test

libraryDependencies += "com.dimafeng" %% "testcontainers-scala" % "0.27.0" % Test
libraryDependencies += "org.testcontainers" % "postgresql" % "1.12.3" % Test

libraryDependencies += specs2 % Test

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-Xfatal-warnings"
)
