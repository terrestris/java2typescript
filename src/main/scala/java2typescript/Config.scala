package java2typescript

case class ImportLocation(
  `class`: String,
  location: String
)

case class ReplacementConfig(
  pattern: String,
  replacement: String
)

case class Config(
  source: String = "",
  target: String = "",
  imports: Option[List[ImportLocation]] = None,
  replacements: List[ReplacementConfig] = List(),
  skipFiles: List[String] = List()
)
