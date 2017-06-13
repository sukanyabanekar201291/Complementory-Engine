name := "engine-complementory"
organization := "io.prediction"

scalaVersion := "2.10.5"

organization := "io.prediction"

libraryDependencies ++= Seq(
  //"io.prediction"    %% "core"     % pioVersion.value % "provided",
  "io.prediction"    %% "core"          % "0.9.6",
  //"org.apache.spark" %% "spark-core"    % "1.3.0" % "provided",
  //"org.apache.spark" %% "spark-mllib"   % "1.3.0" % "provided",
  //"org.apache.spark" %% "spark-core"    % "2.0.0",
  //"org.apache.spark" %% "spark-mllib"   % "2.0.0",
  "org.apache.spark" % "spark-core_2.10" % "1.4.1",
  "org.apache.spark" % "spark-mllib_2.10" % "1.4.1",
  "org.scalatest"    %% "scalatest"     % "2.2.1" % "test",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.15"
)


cancelable in Global := true

mainClass in Compile := Some("ServerApp")

enablePlugins(JavaAppPackaging)
