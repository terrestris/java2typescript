package java2typescript.analyseExports

class Import(
  val packageName: Option[String],
  val javaScope: Option[String],
  val javaName: String,
  val fixedPath: Option[String] = None
) {
  def getTypescriptFile: String =
    javaScope match {
      case s: Some[String] => s"${s.get}.ts"
      case None => s"$javaName.ts"
    }
  def getTypescriptImport: String =
    javaName
}
