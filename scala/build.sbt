val parboiledVersion = "2.4.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "probrogs",
    description := "Scala API for the probrogs benchmark",
    version := "0.1.0",
    scalaVersion := "3.2.2",
    scalacOptions ++= Seq("-deprecation"),
    libraryDependencies += "com.lihaoyi" %% "pythonparse" % "3.0.1"
  )
