organization  := "com.tropius"

version       := "0.1"

scalaVersion  := "2.11.6"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.3.9"
  val sprayV = "1.3.3"
  Seq(
    "io.spray"                      %%  "spray-can"         % sprayV,
    "io.spray"                      %%  "spray-routing"     % sprayV,
    "io.spray"                      %%  "spray-testkit"     % sprayV    % "test",
    "io.spray"                      %%  "spray-client"      % sprayV,
    "io.spray"                      %%  "spray-json"        % "1.3.2",
    "com.typesafe.akka"             %%  "akka-actor"        % akkaV,
    "com.typesafe.akka"             %%  "akka-testkit"      % akkaV     % "test",
    "org.scalaj"                    %%  "scalaj-http"       % "1.1.5",
    "org.specs2"                    %%  "specs2-core"       % "2.3.11"  % "test",
    "com.rockymadden.stringmetric"  %%  "stringmetric-core" % "0.27.4"
  )
}

Revolver.settings
