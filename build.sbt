name := "pfsdm"

version := "1.0"

scalaVersion := "2.10.4"

resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

libraryDependencies ++= Seq(
  "org.lemurproject.galago" % "contrib" % "3.9-SNAPSHOT",
  "org.apache.lucene" % "lucene-core" % "4.8.1",
  "org.apache.lucene" % "lucene-analyzers-common" % "4.8.1",
  "org.functionaljava" % "functionaljava" % "4.3",
  "org.functionaljava" % "functionaljava-java8" % "4.3",
  "org.scalatest" %% "scalatest" % "2.2.5" % "test",
  "org.apache.spark" %% "spark-core" % "1.2.0" % "provided"
)

javacOptions ++= Seq("-Xlint:unchecked")

assemblyMergeStrategy in assembly := {
  case PathList("org", "tartarus", "snowball", xs @ _*)         => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

logLevel in runMain := Level.Error
