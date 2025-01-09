package java2typescript.ast

case class ClassDeclaration(
  name: Identifier,
  typeParameters: List[Type] = List(),
  heritageClauses: List[HeritageClause] = List(),
  members: List[Member] = List(),
  modifiers: List[Modifier] = List()
) extends Statement {
  val kind: SyntaxKind = SyntaxKind.ClassDeclaration
}

case class VariableDeclaration(
  name: Identifier,
  `type`: Option[Type],
  initializer: Option[Expression] = None
) extends Statement {
  val kind: SyntaxKind = SyntaxKind.VariableDeclaration
}

case class InterfaceDeclaration(
  name: Identifier,
  typeParameters: List[Type] = List(),
  heritageClauses: List[HeritageClause] = List(),
  members: List[Member] = List(),
  modifiers: List[Modifier] = List()
) extends Statement {
  val kind: SyntaxKind = SyntaxKind.InterfaceDeclaration
}

case class EnumDeclaration(
  name: Identifier,
  members: List[Member] = List(),
  modifiers: List[Modifier] = List()
) extends Statement {
  val kind: SyntaxKind = SyntaxKind.EnumDeclaration
}

case class ImportDeclaration(
  importClause: ImportClause,
  moduleSpecifier: StringLiteral
) extends Node {
  val kind: SyntaxKind = SyntaxKind.ImportDeclaration
}

case class FunctionDeclaration(
  name: Identifier,
  `type`: Option[Type],
  parameters: List[Parameter] = List(),
  typeParameters: List[Type] = List(),
  body: Option[Block] = None,
  modifiers: List[Modifier] = List(),
) extends Statement {
  val kind: SyntaxKind = SyntaxKind.FunctionDeclaration
}