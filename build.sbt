name := """play-scala-opinionated-backend-seed"""

version := "2.7.x"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.8"

libraryDependencies += guice

libraryDependencies += "org.postgresql" % "postgresql" % "42.2.5"

libraryDependencies += "com.typesafe.play" %% "play-slick" % "4.0.0"
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "4.0.0"

libraryDependencies += "com.github.tminglei" %% "slick-pg" % "0.17.2"
libraryDependencies += "com.github.tminglei" %% "slick-pg_play-json" % "0.17.2"

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.1" % Test

libraryDependencies += "com.dimafeng" %% "testcontainers-scala" % "0.22.0" % Test
libraryDependencies += "org.testcontainers" % "postgresql" % "1.10.1" % Test

libraryDependencies += specs2 % Test

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-Xfatal-warnings"
)
