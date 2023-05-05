val parboiledVersion = "2.4.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "probros",
    description := "Scala API for the probrogs benchmark",
    version := "0.1.0",
    scalaVersion := "3.2.2",
    exportJars := true,
    scalacOptions ++= Seq("-deprecation"),
    libraryDependencies += "com.lihaoyi" %% "pythonparse" % "3.0.1",
    libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.15.3" % Test,
    Compile / resourceDirectory := baseDirectory.value / ".." / "data"
  )


