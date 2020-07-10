name := "feature"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
    ("org.apache.hadoop" % "hadoop-client" % "2.7.2")//.
)

libraryDependencies += "org.json4s" % "json4s-native_2.11" % "3.4.0"
libraryDependencies += "org.apache.kafka" % "kafka-clients" % "0.9.0.1"
libraryDependencies += "org.apache.commons" % "commons-dbcp2" % "2.1.1"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.30"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.22"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.22"

libraryDependencies += "org.elasticsearch" % "elasticsearch" % "2.1.0"
// libraryDependencies += "com.sksamuel.elastic4s" % "elastic4s-core_2.11" % "2.1.0"

libraryDependencies ++= Seq(
    "net.debasishg" %% "redisclient" % "3.0"
)
scalacOptions += "-deprecation"

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
{
    case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
    case PathList("javax", "activation", xs @ _*) => MergeStrategy.last
    case PathList("org", "apache", xs @ _*) => MergeStrategy.last
    case PathList("com", "google", xs @ _*) => MergeStrategy.last
    case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
    case PathList("com", "codahale", xs @ _*) => MergeStrategy.last
    case PathList("com", "yammer", xs @ _*) => MergeStrategy.last
    case "about.html" => MergeStrategy.rename
    case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
    case "META-INF/mailcap" => MergeStrategy.last
    case "META-INF/mimetypes.default" => MergeStrategy.last
    case "plugin.properties" => MergeStrategy.last
    case "log4j.properties" => MergeStrategy.last
    case x if x.endsWith("io.netty.versions.properties") => MergeStrategy.first
    case PathList("org", "slf4j", xs @ _*)         => MergeStrategy.first  
    case PathList("org", "joda", "time", "base", "BaseDateTime.class") => MergeStrategy.first

    case x => old(x)
}
}

