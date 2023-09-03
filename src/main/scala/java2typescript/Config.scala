package de.terrestris
package java2typescript

case class ImportLocation(
  `class`: String,
  location: String
)

case class Config(
  source: String = "",
  target: String = "",
  imports: Option[List[ImportLocation]] = None
)
