import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28" % "7.12.0"
  )

  val test: Seq[ModuleID]    = Seq(
    "uk.gov.hmrc"         %% "bootstrap-test-play-28"   % "7.12.0",
    "org.mockito"         %% "mockito-scala"            % "1.17.12",
    "org.scalatestplus"   %% "scalatestplus-scalacheck" % "3.1.0.0-RC2",
    "com.danielasfregola" %% "random-data-generator"    % "2.9"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test

}
