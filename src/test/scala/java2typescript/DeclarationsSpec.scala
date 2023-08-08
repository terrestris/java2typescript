package de.terrestris
package java2typescript

import java2typescript.parser.parseMethodBody
import java2typescript.writer.write

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class DeclarationsSpec extends AnyFlatSpec with should.Matchers {
  "The transformer" should "transform a string variable declarations" in {
    val input = wrapStatementJava("String s = \"abc\";")
    val expected = "let s: string = \"abc\";\n"
    val parsed = parseMethodBody(input)
    val written = write(parsed)
    written should be(expected)
  }

  "The transformer" should "transform an integer variable declarations" in {
    val input = wrapStatementJava("Integer i = 123;")
    val expected = "let i: number = 123;\n"
    val parsed = parseMethodBody(input)
    val written = write(parsed)
    written should be(expected)
  }

  "The transformer" should "transform a custom type variable declarations" in {
    val input = wrapStatementJava("CustomType c = new CustomType();")
    val expected = "let c: CustomType = new CustomType();\n"
    val parsed = parseMethodBody(input)
    val written = write(parsed)
    written should be(expected)
  }

  "The transformer" should "transform a generic custom type variable declarations with arguments" in {
    val input = wrapStatementJava("CustomType<String> c = new CustomType<>(123, \"abc\");")
    val expected = "let c: CustomType<string> = new CustomType(123, \"abc\");\n"
    val parsed = parseMethodBody(input)
    val written = write(parsed)
    written should be(expected)
  }
}
