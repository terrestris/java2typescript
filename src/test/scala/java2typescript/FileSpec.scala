package de.terrestris
package java2typescript

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class FileSpec extends AnyFlatSpec with should.Matchers {
  "The transformer" should "transform an empty file" in {
    val input = ""
    val parsed = parser.parse(input)
    val written = writer.write(parsed)
    written should fullyMatch regex """\s*"""
  }

  "The transformer" should "transform a file with a class" in {
    val input = "class A {}"
    val parsed = parser.parse(input)
    val written = writer.write(parsed)
    written should fullyMatch regex """export class A \{\s*\}\s*"""
  }

  "The transformer" should "transform a file with an import" in {
    val input = "package a.b.c;\nimport a.b.D;\n"
    val parsed = parser.parse(input)
    val written = writer.write(parsed)
    written should fullyMatch regex """import \{ D \} from \"../D.ts\";\s*"""
  }

  "The transformer" should "transform a file with a class and an import" in {
    val input = "package a.b.c;\nimport a.b.D;\nclass A {}"
    val parsed = parser.parse(input)
    val written = writer.write(parsed)
    written should fullyMatch regex """import \{ D \} from \"../D.ts\";\s*export class A \{\s*\}\s*"""
  }
}
