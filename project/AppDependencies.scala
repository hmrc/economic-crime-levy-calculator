import sbt.*

object AppDependencies {

  private val hmrcBootstrapVersion = "10.5.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-30" % hmrcBootstrapVersion
  )

  val test: Seq[ModuleID]    = Seq(
    "uk.gov.hmrc"         %% "bootstrap-test-play-30"   % hmrcBootstrapVersion,
    "org.mockito"         %% "mockito-scala"            % "2.0.0",
    "org.scalatestplus"   %% "scalacheck-1-17"          % "3.2.18.0",
    "com.danielasfregola" %% "random-data-generator"    % "2.9"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}