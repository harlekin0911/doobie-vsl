//scalaVersion := "2.12.7" // Scala 2.11/12
scalaVersion := "2.12.10" 

scalacOptions += "-Ypartial-unification" // 2.11.9+

lazy val doobieVersion = "0.6.0"

libraryDependencies ++= Seq(
  "org.tpolecat"    %% "doobie-core"     % doobieVersion,
  "org.tpolecat"    %% "doobie-postgres" % doobieVersion,
  "org.tpolecat"    %% "doobie-specs2"   % doobieVersion,
  "io.monix"        %% "monix"           % "3.0.0",
  "org.scalatest"   %% "scalatest"       % "3.2.0-M1" % Test
//,  "com.ibm.db2.jcc" %% "db2jcc4"         % "10.1"


)
