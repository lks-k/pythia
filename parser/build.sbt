val parboiledVersion = "2.4.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "probrogs",
    description := "Scala API for the probrogs benchmark",
    version := "0.1.0",
    scalaVersion := "3.2.2",
    scalacOptions ++= Seq("-deprecation"),
    libraryDependencies += "org.parboiled" %% "parboiled" % "2.4.1"
  )

// lazy val root = (project in file("."))
//   .settings(
//     name := "probrogs",
//     version := "0.1",
//     scalaVersion := "3.2.2",
//     libraryDependencies ++= Seq(
//       "org.parboiled" %% "parboiled2" % parboiledVersion
//     )
//   )
