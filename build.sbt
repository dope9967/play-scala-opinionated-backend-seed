name := """play-scala-opinionated-backend-seed"""

version := "1.0.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.10"

resolvers += "Atlassian Releases" at "https://maven.atlassian.com/public/"

libraryDependencies += guice

libraryDependencies += "org.postgresql" % "postgresql" % "42.2.9"

val playSlickVersion = "4.0.2"
libraryDependencies += "com.typesafe.play" %% "play-slick"            % playSlickVersion
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % playSlickVersion

val slickPgVersion = "0.17.3"
libraryDependencies += "com.github.tminglei" %% "slick-pg"           % slickPgVersion
libraryDependencies += "com.github.tminglei" %% "slick-pg_play-json" % slickPgVersion

val silhouetteVersion = "6.1.0"
libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette"                 % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-password-bcrypt" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-crypto-jca"      % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-persistence"     % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-testkit"         % silhouetteVersion % Test
)

libraryDependencies += "com.iheart" %% "ficus" % "1.4.3"

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % Test

libraryDependencies += "com.dimafeng"       %% "testcontainers-scala" % "0.27.0" % Test
libraryDependencies += "org.testcontainers" % "postgresql"            % "1.12.3" % Test

libraryDependencies += specs2 % Test

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-Xfatal-warnings"
)
