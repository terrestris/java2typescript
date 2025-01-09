package java2typescript.ast

trait Node {
  val kind: SyntaxKind
  val `flags`: Int = 0
}



trait Modifier extends Node

trait Type extends Node

case class Identifier(escapedText: String) extends Expression {
  val kind: SyntaxKind = SyntaxKind.Identifier
}

case class Parameter(
  name: Identifier,
  `type`: Option[Type],
  modifiers: List[Modifier] = List(),
  dotDotDotToken: Option[DotDotDotToken] = None
) extends Node {
  val kind: SyntaxKind = SyntaxKind.Parameter
}

case class TypeReference(
  typeName: Identifier,
  typeArguments: List[Type] = List()
) extends Type {
  val kind: SyntaxKind = SyntaxKind.TypeReference
}

case class ImportClause(
  namedBindings: NamedImports
) extends Node {
  val kind: SyntaxKind = SyntaxKind.ImportClause
  val isTypeOnly = false
}

case class HeritageClause(
  types: List[ExpressionWithTypeArguments],
  token: SyntaxKind
) extends Node {
  val kind: SyntaxKind = SyntaxKind.HeritageClause
}

case class CatchClause(
  variableDeclaration: VariableDeclaration,
  block: Block
) extends Node {
  val kind: SyntaxKind = SyntaxKind.CatchClause
}

case class NamedImports(
  elements: List[ImportSpecifier]
) extends Node {
  val kind: SyntaxKind = SyntaxKind.NamedImports
}

case class ImportSpecifier(
  name: Identifier
) extends Node {
  val kind: SyntaxKind = SyntaxKind.ImportSpecifier
  val isTypeOnly = false
}

case class ArrayType(
  elementType: Type
) extends Type {
  val kind: SyntaxKind = SyntaxKind.ArrayType
}

case class CaseBlock(
  clauses: List[CaseClause|DefaultClause]
) extends Node {
  val kind: SyntaxKind = SyntaxKind.CaseBlock
}

case class CaseClause(
  expression: Expression,
  statements: List[Statement]
) extends Node {
  val kind: SyntaxKind = SyntaxKind.CaseClause
}

case class DefaultClause(
  statements: List[Statement]
) extends Node {
  val kind: SyntaxKind = SyntaxKind.DefaultClause
}








