package de.terrestris
package java2typescript

import java2typescript.parser.parseMethodBody
import java2typescript.writer.{serialize, write}

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class ExpressionsSpec extends AnyFlatSpec with should.Matchers {
  "The transformer" should "transform addition" in {
    val input = wrapStatementJava("Double a = 2 + 3;")
    val expected = "let a: number = 2 + 3;\n"
    val parsed = parseMethodBody(input)
    val written = write(parsed)
    written should be(expected)
  }

  "The transformer" should "transform subtraction" in {
    val input = wrapStatementJava("Double a = 2 - 3;")
    val expected = "let a: number = 2 - 3;\n"
    val parsed = parseMethodBody(input)
    val written = write(parsed)
    written should be(expected)
  }

  "The transformer" should "transform multiplication" in {
    val input = wrapStatementJava("Double a = 2 * 3;")
    val expected = "let a: number = 2 * 3;\n"
    val parsed = parseMethodBody(input)
    val written = write(parsed)
    written should be(expected)
  }

  "The transformer" should "transform division" in {
    val input = wrapStatementJava("Double a = 2 / 3;")
    val expected = "let a: number = 2 / 3;\n"
    val parsed = parseMethodBody(input)
    val written = write(parsed)
    written should be(expected)
  }

  "The transformer" should "transform negative numbers" in {
    val input = wrapStatementJava("Double a = -3;")
    val expected = "let a: number = -3;\n"
    val parsed = parseMethodBody(input)
    val written = write(parsed)
    written should be(expected)
  }

  "The transformer" should "transform parenthesis" in {
    val input = wrapStatementJava("Double a = (3);")
    val expected = "let a: number = (3);\n"
    val parsed = parseMethodBody(input)
    val written = write(parsed)
    written should be(expected)
  }

  "The transformer" should "transform mathematical expressions" in {
    val input = wrapStatementJava("Double a = -2 + 3 - 4 / 5 * (6 + 7);")
    val expected = "let a: number = -2 + 3 - 4 / 5 * (6 + 7);\n"
    val parsed = parseMethodBody(input)
    val written = write(parsed)
    written should be(expected)
  }
}
