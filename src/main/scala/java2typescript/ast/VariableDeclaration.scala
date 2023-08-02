package de.terrestris
package java2typescript.ast

class VariableDeclaration(
  val name: Identifier,
  //  exclamationToken: Null,
  val `type`: Keyword,
  val initializer: Literal,
  val kind: Int = 259
) extends Node

