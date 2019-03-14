scalaVersion := "2.12.7" // Scala 2.11/12

scalacOptions += "-Ypartial-unification" // 2.11.9+

lazy val doobieVersion = "0.6.0"

libraryDependencies ++= Seq(
  "org.tpolecat" %% "doobie-core"     % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-specs2"   % doobieVersion
)
