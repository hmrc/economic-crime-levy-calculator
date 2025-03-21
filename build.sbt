import play.sbt.routes.RoutesKeys
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings

val appName = "economic-crime-levy-calculator"

ThisBuild / scalaVersion := "2.13.16"
ThisBuild / majorVersion := 0

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(inConfig(Test)(testSettings) *)
  .settings(inThisBuild(buildSettings))
  .settings(scoverageSettings *)
  .settings(scalaCompilerOptions *)
  .settings(
    name := appName,
    RoutesKeys.routesImport ++= Seq(
      "uk.gov.hmrc.economiccrimelevycalculator.models._",
      "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl"
    ),
    PlayKeys.playDefaultPort := 14010,
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    (update / evictionWarningOptions).withRank(KeyRanks.Invisible) :=
      EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    resolvers ++= Seq(Resolver.jcenterRepo)
  )

lazy val buildSettings = Def.settings(
  scalafmtOnCompile := true,
  useSuperShell := false
)

lazy val testSettings: Seq[Def.Setting[?]] = Seq(
  unmanagedSourceDirectories := Seq(
    baseDirectory.value / "test",
    baseDirectory.value / "test-common"
  ),
  fork := true
)

lazy val it = project
  .enablePlugins(play.sbt.PlayScala)
  .dependsOn(root % "test -> test")
  .settings(
    DefaultBuildSettings.itSettings(),
    unmanagedSourceDirectories := Seq(
      baseDirectory.value / "it",
      baseDirectory.value / "test-common"
    ),
    fork := true
  )

val excludedScoveragePackages: Seq[String] = Seq(
  "<empty>",
  "Reverse.*",
  ".*handlers.*",
  "uk.gov.hmrc.BuildInfo",
  "app.*",
  "prod.*",
  ".*Routes.*",
  "testOnly.*",
  "testOnlyDoNotUseInAppConf.*"
)

val scoverageSettings: Seq[Setting[?]] = Seq(
  ScoverageKeys.coverageExcludedFiles := excludedScoveragePackages.mkString(";"),
  ScoverageKeys.coverageMinimumStmtTotal := 90,
  ScoverageKeys.coverageFailOnMinimum := true,
  ScoverageKeys.coverageHighlighting := true
)

val scalaCompilerOptions: Def.Setting[Task[Seq[String]]] = scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-rootdir",
  baseDirectory.value.getCanonicalPath,
  "-Wconf:cat=feature:ws,cat=optimizer:ws,src=target/.*:s",
  "-Xlint:-byname-implicit"
)

addCommandAlias("runAllChecks", ";clean;compile;scalafmtCheckAll;coverage;test;it:test;scalastyle;coverageReport")
