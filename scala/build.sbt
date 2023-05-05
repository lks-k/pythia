val parboiledVersion = "2.4.1"

// Compute a list of files to package inside a jar
lazy val pyFiles = new File("../data/")
  .listFiles()
  .toSeq
  .map { _.getName() }
  .filter { _.takeRight(3) == ".py" }

lazy val root = project
  .in(file("."))
  .settings(
    name := "probrogs",
    description := "Scala API for the probrogs benchmark",
    version := "0.1.0",
    scalaVersion := "3.2.2",
    scalacOptions ++= Seq("-deprecation"),
    libraryDependencies += "com.lihaoyi" %% "pythonparse" % "3.0.1",
    libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.15.3" % Test,
    Compile / packageBin / mappings ++= 
      pyFiles.map { f => 
        ( baseDirectory.value / ".." / "data" / f ) -> s"/data/$f" }
  )


