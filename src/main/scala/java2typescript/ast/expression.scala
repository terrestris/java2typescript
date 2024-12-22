package java2typescript.ast

trait Expression extends Node

case class ArrayLiteralExpression(
  elements: List[Expression]
) extends Expression {
  val kind: SyntaxKind = SyntaxKind.ArrayLiteralExpression
}

case class PropertyAccessExpression(
  expression: Expression,
  name: Identifier
) extends Expression {
  val kind: SyntaxKind = SyntaxKind.PropertyAccessExpression
}

case class ElementAccessExpression(
  expression: Expression,
  argumentExpression: Expression
) extends Expression {
  val kind: SyntaxKind = SyntaxKind.ElementAccessExpression
}

case class CallExpression(
  expression: Expression,
  arguments: List[Expression] = List(),
  typeArguments: List[Type] = List()
) extends Expression {
  val kind: SyntaxKind = SyntaxKind.CallExpression
}

case class NewExpression (
  expression: Identifier,
  arguments: List[Expression] = List(),
  typeArguments: List[Type] = List()
) extends Expression {
  val kind: SyntaxKind = SyntaxKind.NewExpression
}

case class ParenthesizedExpression(
  expression: Expression
) extends Expression {
  val kind: SyntaxKind = SyntaxKind.ParenthesizedExpression
}

case class TypeOfExpression(
  expression: Expression
) extends Expression {
  val kind: SyntaxKind = SyntaxKind.TypeOfExpression
}

case class PrefixUnaryExpression(
  operator: SyntaxKind,
  operand: Expression
) extends Expression {
  val kind: SyntaxKind = SyntaxKind.PrefixUnaryExpression
}

case class PostfixUnaryExpression(
  operator: SyntaxKind,
  operand: Expression
) extends Expression {
  val kind: SyntaxKind = SyntaxKind.PostfixUnaryExpression
}

case class BinaryExpression(
  left: Expression,
  right: Expression,
  operatorToken: Token
) extends Expression {
  val kind: SyntaxKind = SyntaxKind.BinaryExpression
}

case class ConditionalExpression(
  condition: Expression,
  whenTrue: Expression,
  whenFalse: Expression,
) extends Expression {
  val questionToken: QuestionToken = QuestionToken()
  val colonToken: ColonToken = ColonToken()
  val kind: SyntaxKind = SyntaxKind.ConditionalExpression
}

case class ExpressionWithTypeArguments(
  expression: Expression,
  typeArguments: List[Type] = List()
) extends Expression {
  val kind: SyntaxKind = SyntaxKind.ExpressionWithTypeArguments
}

case class AsExpression(
  expression: Expression,
  `type`: Type
) extends Expression {
  val kind: SyntaxKind = SyntaxKind.AsExpression
}

case class VariableDeclarationList(
  declarations: List[VariableDeclaration],
  override val flags: Int = 1 // 1 = Let
) extends Expression {
  val kind: SyntaxKind = SyntaxKind.VariableDeclarationList
}
