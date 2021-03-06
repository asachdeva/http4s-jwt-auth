import com.scalapenos.sbt.prompt.SbtPrompt.autoImport._
import com.scalapenos.sbt.prompt._
import Dependencies._
import microsites.ExtraMdFileConfig

ThisBuild / organization := "dev.profunktor"
ThisBuild / organizationName := "ProfunKtor"

crossScalaVersions in ThisBuild := Seq("2.12.10", "2.13.1")

promptTheme := PromptTheme(
  List(
    text("[sbt] ", fg(105)),
    text(_ => "http4s-jwt-auth", fg(15)).padRight(" λ ")
  )
)

def maxClassFileName(v: String) = CrossVersion.partialVersion(v) match {
  case Some((2, 13)) => Seq.empty[String]
  case _             => Seq("-Xmax-classfile-name", "100")
}

val commonSettings = Seq(
  organizationName := "Opinionated JWT authentication library for Http4s",
  startYear := Some(2019),
  licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),
  homepage := Some(url("https://http4s-jwt-auth.profunktor.dev/")),
  headerLicense := Some(HeaderLicense.ALv2("2019", "ProfunKtor")),
  resolvers += "Apache public" at "https://repository.apache.org/content/groups/public/",
  scalacOptions ++= maxClassFileName(scalaVersion.value),
  scalafmtOnCompile := true,
  publishTo := {
      val sonatype = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at sonatype + "content/repositories/snapshots")
      else
        Some("releases" at sonatype + "service/local/staging/deploy/maven2")
    },
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ =>
      false
    },
  pomExtra :=
      <developers>
        <developer>
          <id>gvolpe</id>
          <name>Gabriel Volpe</name>
          <url>https://github.com/gvolpe</url>
        </developer>
      </developers>
)

lazy val noPublish = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  skip in publish := true
)

lazy val root = (project in file("."))
  .aggregate(core, microsite)
  .settings(noPublish)

lazy val core = (project in file("core"))
  .settings(
    name := "http4s-jwt-auth",
    libraryDependencies ++= Seq(
          CompilerPlugins.kindProjector,
          CompilerPlugins.betterMonadicFor,
          Libraries.cats,
          Libraries.catsEffect,
          Libraries.fs2,
          Libraries.http4sDsl,
          Libraries.http4sServer,
          Libraries.jwtCore,
          Libraries.scalaTest % Test
        )
  )
  .settings(commonSettings: _*)

lazy val microsite = project
  .in(file("site"))
  .enablePlugins(MicrositesPlugin)
  .settings(commonSettings: _*)
  .settings(noPublish)
  .settings(publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true))
  .settings(
    micrositeName := "Http4s Jwt Auth",
    micrositeDescription := "Opinionated JWT authentication library for Http4s",
    micrositeAuthor := "ProfunKtor",
    micrositeGithubOwner := "profunktor",
    micrositeGithubRepo := "http4s-jwt-auth",
    micrositeBaseUrl := "",
    micrositeExtraMdFiles := Map(
          file("README.md") -> ExtraMdFileConfig(
                "index.md",
                "home",
                Map("title" -> "Home", "position" -> "0")
              ),
          file("CODE_OF_CONDUCT.md") -> ExtraMdFileConfig(
                "CODE_OF_CONDUCT.md",
                "page",
                Map("title" -> "Code of Conduct")
              )
        ),
    micrositeExtraMdFilesOutput := (resourceManaged in Compile).value / "jekyll",
    micrositeGitterChannel := true,
    micrositeGitterChannelUrl := "profunktor-dev/http4s-jwt-auth",
    micrositePushSiteWith := GitHub4s,
    micrositeGithubToken := sys.env.get("GITHUB_TOKEN"),
    scalacOptions --= Seq(
          "-Werror",
          "-Xfatal-warnings",
          "-Ywarn-unused-import",
          "-Ywarn-numeric-widen",
          "-Ywarn-dead-code",
          "-Xlint:-missing-interpolator,_"
        )
  )
  .dependsOn(core)

// CI build
addCommandAlias("fullBuild", ";clean;+test;mdoc")
