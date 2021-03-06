//scalaVersion := "2.12.7" // Scala 2.11/12
//scalaVersion := "2.13.1" 
scalaVersion := "2.12.10" 
//scalaVersion := "2.13.3" 

scalacOptions += "-Ypartial-unification" // 2.11.9+
scalacOptions ++= Seq("-deprecation", "-feature")

//lazy val doobieVersion = "0.6.0"
lazy val doobieVersion = "0.9.0"
lazy val metricsVersion = "3.0.2"

libraryDependencies ++= Seq(
    "org.tpolecat"    %% "doobie-core"     % doobieVersion
  , "org.tpolecat"    %% "doobie-postgres" % doobieVersion
  , "org.tpolecat"    %% "doobie-specs2"   % doobieVersion
  , "org.tpolecat"    %% "doobie-hikari"   % doobieVersion  
  , "io.monix"        %% "monix"           % "3.2.2"
  , "org.scalatest"   %% "scalatest"       % "3.2.0-M1" % Test
  , "com.typesafe"    % "config"           % "1.4.0"
//, "com.zaxxer"      % "HikariCP"         % "3.4.2"
//, "com.ibm.db2.jcc" %% "db2jcc4"         % "10.1"

)

libraryDependencies += "com.codahale.metrics" % "metrics-healthchecks" % metricsVersion
libraryDependencies += "com.typesafe" % "config" % "1.4.0"
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.30"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.30"
libraryDependencies += "com.codahale.metrics" % "metrics-core" % metricsVersion
libraryDependencies += "com.codahale.metrics" % "metrics-jvm" % metricsVersion

import Tests._

{
  def groupByFirst(tests: Seq[TestDefinition]) =
    tests groupBy (_.name(4)) map {
      case (letter, tests) =>
        val options = ForkOptions().withRunJVMOptions(Vector("-Dfirst.letter"+letter))
        new Group(letter.toString, tests, SubProcess(options))
    } toSeq

    Test / testGrouping := groupByFirst( (Test / definedTests).value )
}