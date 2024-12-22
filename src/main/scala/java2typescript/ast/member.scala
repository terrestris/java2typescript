package java2typescript.ast

trait Member extends Node

case class PropertyDeclaration(
  name: Identifier,
  `type`: Option[Type],
  initializer: Option[Expression],
  modifiers: List[Modifier] = List()
) extends Member {
  val kind: SyntaxKind = SyntaxKind.PropertyDeclaration
}

case class MethodDeclaration(
  name: Identifier,
  `type`: Option[Type],
  parameters: List[Parameter] = List(),
  typeParameters: List[Type] = List(),
  body: Option[Block] = None,
  modifiers: List[Modifier] = List(),
) extends Member {
  val kind: SyntaxKind = SyntaxKind.MethodDeclaration
}

case class Constructor(
  parameters: List[Parameter] = List(),
  body: Option[Block] = None,
  modifiers: List[Modifier] = List(),
) extends Member {
  val kind: SyntaxKind = SyntaxKind.Constructor
  override val flags: Int = 66048
}

case class EnumMember(
  name: Identifier
) extends Member {
  val kind: SyntaxKind = SyntaxKind.EnumMember
}
