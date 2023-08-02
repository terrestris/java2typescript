package de.terrestris
package java2typescript.ast

class VariableDeclarationList(
  val declarations: List[VariableDeclaration],
  override val flags: Int = 2, // 2 = Const
  val kind: Int = 260
) extends Node
