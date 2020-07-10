name := "UfsFromBigData"

version := "1.0"

// scalaVersion := "2.11.8"
scalaVersion := "2.10.6"

libraryDependencies ++= Seq(
    ("org.apache.hadoop" % "hadoop-client" % "2.7.2")//.
)

libraryDependencies += "com.alibaba" % "fastjson" % "1.2.23"

libraryDependencies += "org.apache.kafka" % "kafka-clients" % "0.9.0.1"
libraryDependencies += "org.apache.commons" % "commons-dbcp2" % "2.1.1"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.30"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.22"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.22"

libraryDependencies += "org.elasticsearch" % "elasticsearch" % "2.1.0"

// libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "1.5.0" % "provided"
// libraryDependencies += "org.apache.spark" % "spark-sql_2.11" % "1.5.0" % "provided"
// libraryDependencies += "org.apache.spark" % "spark-hive_2.11" % "1.5.0" % "provided"
libraryDependencies += "org.apache.spark" % "spark-core_2.10" % "1.5.0" % "provided"
// libraryDependencies += "org.apache.spark" % "spark-sql_2.10" % "1.5.0" % "provided"
// libraryDependencies += "org.apache.spark" % "spark-hive_2.10" % "1.5.0" % "provided"


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
    case "overview.html" => MergeStrategy.last
    case "parquet.thrift" => MergeStrategy.last
    case "plugin.xml" => MergeStrategy.last
    case x if x.endsWith("io.netty.versions.properties") => MergeStrategy.first
    case PathList("org", "slf4j", xs @ _*)         => MergeStrategy.first  
    case PathList("org", "joda", "time", "base", "BaseDateTime.class") => MergeStrategy.first
    case PathList("javax", "xml", xs @ _*) => MergeStrategy.first

    // case PathList("javax", "xml", "stream", xs @ _*) => MergeStrategy.first

    case x => old(x)
}
}

