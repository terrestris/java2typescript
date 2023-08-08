ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.0"

lazy val root = (project in file("."))
  .settings(
    name := "java2typescript",
    idePackagePrefix := Some("de.terrestris"),
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.16",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.16" % "test",
    libraryDependencies += "com.github.javaparser" % "javaparser-core" % "3.25.4",
    libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.15.1"
  )
