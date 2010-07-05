import sbt._

class FooProject(info: ProjectInfo) extends DefaultProject(info) {
  // http://macstrac.blogspot.com/2010/01/using-sbt-on-your-scala-maven-project.html
  val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"
}
