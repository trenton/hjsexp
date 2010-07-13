import sbt._

class UseMavenProject(info: ProjectInfo) extends DefaultProject(info) {
  // copied the magics from
  // http://macstrac.blogspot.com/2010/01/using-sbt-on-your-scala-maven-project.html
  val mavenLocal = "Local Maven Repository" at "file://" + Path.userHome + "/.m2/repository"
}
