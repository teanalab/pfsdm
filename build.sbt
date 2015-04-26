name := "fsdm2"

version := "1.0"

scalaVersion := "2.11.6"

resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

libraryDependencies += "org.lemurproject.galago" % "contrib" % "3.8-SNAPSHOT"