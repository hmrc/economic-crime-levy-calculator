import sbt.*

object AppDependencies {

  private val hmrcBootstrapVersion = "10.7.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-30" % hmrcBootstrapVersion,
    "ch.qos.logback" % "logback-core"               % "1.5.27",
    "ch.qos.logback" % "logback-classic"            % "1.5.27",
    "org.apache.commons" % "commons-lang3"          % "3.18.0",
    "at.yawk.lz4"    % "lz4-java"                   % "1.10.3"
  )

  val test: Seq[ModuleID]    = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30" % hmrcBootstrapVersion,
    "org.scalatestplus" %% "mockito-4-11"           % "3.2.18.0",
    "org.scalatestplus" %% "scalacheck-1-17"        % "3.2.18.0"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}