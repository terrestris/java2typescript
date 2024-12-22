package java2typescript.ast

trait Statement extends Node

case class Block(
  statements: List[Statement]
) extends Statement {
  val kind: SyntaxKind = SyntaxKind.Block
  val multiLine: Boolean = true
}

case class VariableStatement(
  declarationList: VariableDeclarationList
) extends Statement {
  val kind: SyntaxKind = SyntaxKind.VariableStatement
}

case class ExpressionStatement(
  expression: Expression
) extends Statement {
  val kind: SyntaxKind = SyntaxKind.ExpressionStatement
}

case class IfStatement(
  expression: Expression,
  thenStatement: Statement,
  elseStatement: Option[Statement] = None
) extends Statement {
  val kind: SyntaxKind = SyntaxKind.IfStatement
}

case class DoStatement(
  statement: Statement,
  expression: Expression
) extends Statement {
  val kind: SyntaxKind = SyntaxKind.DoStatement
}

case class WhileStatement(
  expression: Expression,
  statement: Statement
) extends Statement {
  val kind: SyntaxKind = SyntaxKind.WhileStatement
}

case class ForStatement(
  initializer: Option[Expression],
  condition: Option[Expression],
  incrementor: Option[Expression],
  statement: Statement
) extends Statement {
  val kind: SyntaxKind = SyntaxKind.ForStatement
}

case class ForOfStatement(
  initializer: VariableDeclarationList,
  expression: Expression,
  statement: Statement
) extends Statement {
  val kind: SyntaxKind = SyntaxKind.ForOfStatement
}

case class ContinueStatement() extends Statement {
  val kind: SyntaxKind = SyntaxKind.ContinueStatement
}

case class BreakStatement() extends Statement {
  val kind: SyntaxKind = SyntaxKind.BreakStatement
}

case class ReturnStatement(
  expression: Option[Expression]
) extends Statement {
  val kind: SyntaxKind = SyntaxKind.ReturnStatement
}

case class SwitchStatement(
  expression: Expression,
  caseBlock: CaseBlock
) extends Statement {
  val kind: SyntaxKind = SyntaxKind.SwitchStatement
}

case class ThrowStatement(
  expression: Expression
) extends Statement {
  val kind: SyntaxKind = SyntaxKind.ThrowStatement
}

case class TryStatement(
  tryBlock: Block,
  catchClause: Option[CatchClause],
  finallyBlock: Option[Block]
) extends Statement {
  val kind: SyntaxKind = SyntaxKind.TryStatement
}

case class EmptyStatement() extends Statement {
  val kind: SyntaxKind = SyntaxKind.EmptyStatement
}

