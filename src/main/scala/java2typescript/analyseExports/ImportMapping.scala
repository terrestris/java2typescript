package java2typescript.analyseExports

class ImportMapping(
  val packageName: Option[String],
  val javaQualifiedName: String,
  val typescriptName: String,
  val fixedPath: Option[String] = None
)
