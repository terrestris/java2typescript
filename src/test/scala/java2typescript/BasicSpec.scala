package de.terrestris
package java2typescript

import de.terrestris.java2typescript.writer.write
import de.terrestris.java2typescript.parser.parse

import collection.mutable.Stack
import org.scalatest.*
import flatspec.*
import matchers.*

def wrapStatementJava(code: String) =
  s"class Wrap { void wrap() { $code } }"

//def wrapStatementTypeScript(code: String) =
//  s"class Wrap { method() { $code } }"

class BasicSpec extends AnyFlatSpec with should.Matchers {
  "The write function" should "write a basic expression in typescript" in {
    val input = wrapStatementJava("String s = \"abc\";")
    val expected = "const s: string = \"abc\";\n"
    val parsed = parse(input)
    val written = write(parsed)
    written should be (expected)
  }
}
