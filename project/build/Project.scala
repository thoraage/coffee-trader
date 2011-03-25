import sbt._

class Project(info: ProjectInfo) extends DefaultWebProject(info) {
  def lift(module:String) = "net.liftweb" %% ("lift-" + module) % "2.3-RC3" withSources()
  val liftWebkit   = lift("webkit")
  val liftCommon   = lift("common")
  def dispatch(module:String) = "net.databinder" %% ("dispatch-" + module) % "0.7.8" withSources()
  val dispatchHttp = dispatch("http")
  val specs        = "org.scala-tools.testing" %% "specs" % "1.6.7" % "test" withSources
  val httpClient   = "org.apache.httpcomponents" % "httpclient" % "4.0.1" withSources
  val jodaTime     = "joda-time" % "joda-time" % "1.6.2" withSources

  val jettyTest    = "org.mortbay.jetty" % "jetty" % "6.1.22" % "test" withSources
  override def jettyPort = 8086
}