ThisBuild / scalaVersion := "3.0.2"

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-generic" % "0.14.1",
      "io.circe" %%% "circe-parser" % "0.14.1"
    )
  )

lazy val client = project
  .in(file("client"))
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    Compile / scalaJSLinkerConfig ~= { _.withSourceMap(false) },
    Compile / useYarn := true,
    Compile / npmDependencies ++= Seq(
      "react" -> "17.0.2",
      "react-dom" -> "17.0.2"
    ),
    libraryDependencies ++= Seq(
      ("org.scala-js" %%% "scalajs-dom" % "1.2.0")
        .cross(CrossVersion.for3Use2_13),
      "com.github.japgolly.scalajs-react" %%% "core" % "2.0.0-RC3",
      "com.github.japgolly.scalajs-react" %%% "extra" % "2.0.0-RC3"
    )
  )
  .dependsOn(core.js)

lazy val server = project
  .in(file("server"))
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.4.1",
      "com.lihaoyi" %% "cask" % "0.7.11-2-fb9b10-DIRTY3d489885",
    ),
    Compile / resourceGenerators += Def.task {
      val source = (client / Compile / fastOptJS / webpack).value.head.data
      val dest = (Compile / resourceManaged).value / "assets" / "main.js"
      IO.copy(Seq(source -> dest))
      Seq(dest)
    },
    run / fork := true
  )
  .dependsOn(core.jvm)
