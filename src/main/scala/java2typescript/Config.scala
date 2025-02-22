package java2typescript

import java2typescript.analyseExports.Import

case class ReplacementConfig(
  pattern: String,
  replacement: String
)

class Config(
  val source: String = "",
  val target: String = "",
  val replacements: List[ReplacementConfig] = List(),
  val skipFiles: List[String] = List(),
  val customImports: List[Import] = List()
)