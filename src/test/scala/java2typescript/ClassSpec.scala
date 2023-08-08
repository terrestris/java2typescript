package de.terrestris
package java2typescript

import java2typescript.parser.parse
import java2typescript.writer.write

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class ClassSpec extends AnyFlatSpec with should.Matchers {
  "The transformer" should "transform a class" in {
    val input = "class A {}"
    val parsed = parse(input)
    val written = write(parsed)
    written should fullyMatch regex """export class A \{\s*\}\s*"""
  }

  "The transformer" should "transform an interface" in {
    val input = "interface A {}"
    val parsed = parse(input)
    val written = write(parsed)
    written should fullyMatch regex """export interface A \{\s*\}\s*"""
  }
}
