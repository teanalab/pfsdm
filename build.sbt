name := "pfsdm"

version := "1.0"

scalaVersion := "2.11.6"

resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

libraryDependencies ++= Seq(
  "org.lemurproject.galago" % "contrib" % "3.8-SNAPSHOT",
  "org.functionaljava" % "functionaljava" % "4.3",
  "org.functionaljava" % "functionaljava-java8" % "4.3",
  "org.scalatest" %% "scalatest" % "2.2.5" % "test"
)
