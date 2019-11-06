import sbt.Keys.name


scalaVersion := "2.12.8"

val appName = "geolatte-featureserver"
val appVersion = "2.0-SNAPSHOT"

//Resolvers
lazy val commonResolvers = Seq(
  "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + "/.m2/repository",
  Resolver.typesafeRepo("releases"),
  "Sonatype Repo" at "https://oss.sonatype.org/content/repositories/releases/",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
    //  "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
  Resolver.jcenterRepo,
  Resolver.url( "artifactory", url( "http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases" ) )(
    Resolver
      .ivyStylePatterns
    )
  )

lazy val coreDependencies = Seq(
  "org.geolatte" % "geolatte-geom" % "0.14",
  "commons-codec" % "commons-codec" % "1.8",
  "net.sf.supercsv" % "super-csv" % "2.1.0",
  "org.parboiled" %% "parboiled" % "2.1.4",
  "com.typesafe.akka" %% "akka-slf4j" % "2.5.26",
  "net.logstash.logback" % "logstash-logback-encoder" % "4.6",
  filters,
  guice
  )

lazy val psqlDependencies = Seq(
  "com.typesafe.slick" %% "slick" % "3.2.0",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.0",
  "org.postgresql" % "postgresql" % "42.1.1"
  )

lazy val prometheusClientVersion = "0.0.23"

lazy val prometheusDependencies = Seq(
  "io.prometheus" % "simpleclient" % prometheusClientVersion,
  "io.prometheus" % "simpleclient_common" % prometheusClientVersion,
  "io.prometheus" % "simpleclient_hotspot" % prometheusClientVersion,
  "io.prometheus" % "simpleclient_dropwizard" % prometheusClientVersion
  )

lazy val testDependencies = Seq(
  specs2 % Test
  )


//Dependencies
lazy val dependencies = coreDependencies ++
  psqlDependencies ++
  prometheusDependencies ++
  testDependencies

//Build Settings applied to all projects
lazy val commonBuildSettings = Seq(
  name := appName,
  version := appVersion,
  organization := "org.geolatte",
  scalacOptions ++= Seq( "-feature", "-language:postfixOps", "-language:implicitConversions" , "-deprecation"),
  resolvers ++= commonResolvers
  )

lazy val ItTest = config( "integration" ) extend Test

def itFilter(name: String): Boolean = name startsWith "integration"

def unitFilter(name: String): Boolean = !itFilter( name )

//Settings applied to all projects
lazy val defaultSettings =
  commonBuildSettings ++
    Seq(
      libraryDependencies ++= dependencies,
      Keys.fork in run := true,
      javaOptions in(Test, run) += "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005",
      javacOptions ++= Seq( "-source", "1.8", "-target", "1.8" )
      )

//Options for running tests
val testSettings = Seq(
  Keys.fork in Test := false, //Fork a new JVM for running tests
  testOptions in Test := Seq( Tests.Filter( unitFilter ) ),
  parallelExecution in ItTest := false,
  testOptions in ItTest := Seq( Tests.Argument( "sequential" ), Tests.Filter( itFilter ) )
  )

val main = (project in file("."))
  .settings(
    defaultSettings:_*
    ).configs( ItTest )
  .settings( inConfig( ItTest )( Defaults.testTasks ): _* )
  .settings( (defaultSettings ++ testSettings): _* )
  //    .settings(routesGenerator := InjectedRoutesGenerator)
  .enablePlugins(PlayScala)