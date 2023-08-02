package de.terrestris
package java2typescript.ast


trait Literal extends Node

class NumericLiteral(val text: String, val kind: Int = 9) extends Literal

class StringLiteral(
  val text: String,
  val hasExtendedUnicodeEscape: Boolean = false,
  val kind: Int = 11
) extends Literal
