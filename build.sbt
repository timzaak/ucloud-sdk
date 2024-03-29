name := "ucloud_client"

version := "1.0"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "cn.ucloud.ufile" % "ufile-client-java" % "2.0.4" % Test,
  "org.scalatest" %% "scalatest" % "3.0.1" % Test,
  "com.typesafe" % "config" % "1.3.3" % Test
)

lazy val scalaj_http = RootProject(file("../very-util-scalaj-http"))



lazy val ucloud_client = project.in(file(".")).dependsOn(scalaj_http)
