package java2typescript.ast

trait Node {
  val kind: SyntaxKind
  val `flags`: Int = 0
}

trait Expression extends Node

trait Statement extends Node

trait Token extends Node

trait Modifier extends Node

trait Member extends Node

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
) extends Node {
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

case class ImportDeclaration(
  importClause: ImportClause,
  moduleSpecifier: StringLiteral
) extends Node {
  val kind: SyntaxKind = SyntaxKind.ImportDeclaration
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

// Members

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

// Expression

trait Literal extends Expression

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

// Literal

case class NumericLiteral(text: String) extends Literal {
  val kind: SyntaxKind = SyntaxKind.NumericLiteral
}

case class StringLiteral(
  text: String
) extends Literal {
  val kind: SyntaxKind = SyntaxKind.StringLiteral
  val hasExtendedUnicodeEscape = false
}

// Statement

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

// Keyword

case class ExportKeyword() extends Modifier {
  val kind: SyntaxKind = SyntaxKind.ExportKeyword
}

case class ExtendsKeyword() extends Token {
  val kind: SyntaxKind = SyntaxKind.ExtendsKeyword
}

case class FalseKeyword() extends Literal {
  val kind: SyntaxKind = SyntaxKind.FalseKeyword
}

case class InstanceOfKeyword() extends Token {
  val kind: SyntaxKind = SyntaxKind.InstanceOfKeyword
}

case class NullKeyword() extends Literal {
  val kind: SyntaxKind = SyntaxKind.NullKeyword
}

case class SuperKeyword() extends Expression {
  val kind: SyntaxKind = SyntaxKind.SuperKeyword
}

case class ThisKeyword() extends Expression {
  val kind: SyntaxKind = SyntaxKind.ThisKeyword
}

case class TrueKeyword() extends Literal {
  val kind: SyntaxKind = SyntaxKind.TrueKeyword
}

case class VoidKeyword() extends Type {
  val kind: SyntaxKind = SyntaxKind.VoidKeyword
}

case class ImplementsKeyword() extends Token {
  val kind: SyntaxKind = SyntaxKind.ImplementsKeyword
}

case class PrivateKeyword() extends Modifier {
  val kind: SyntaxKind = SyntaxKind.PrivateKeyword
}

case class ProtectedKeyword() extends Modifier {
  val kind: SyntaxKind = SyntaxKind.ProtectedKeyword
}

case class PublicKeyword() extends Modifier {
  val kind: SyntaxKind = SyntaxKind.PublicKeyword
}

case class StaticKeyword() extends Modifier {
  val kind: SyntaxKind = SyntaxKind.StaticKeyword
}

case class AbstractKeyword() extends Modifier {
  val kind: SyntaxKind = SyntaxKind.AbstractKeyword
}

case class AnyKeyword() extends Type {
  val kind: SyntaxKind = SyntaxKind.AnyKeyword
}

case class BooleanKeyword() extends Type {
  val kind: SyntaxKind = SyntaxKind.BooleanKeyword
}

case class NumberKeyword() extends Type {
  val kind: SyntaxKind = SyntaxKind.NumberKeyword
}

case class StringKeyword() extends Type {
  val kind: SyntaxKind = SyntaxKind.StringKeyword
}

// Token

case class DotDotDotToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.DotDotDotToken
}

case class LessThanToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.LessThanToken
}

case class GreaterThanToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.GreaterThanToken
}

case class LessThanEqualsToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.LessThanEqualsToken
}

case class GreaterThanEqualsToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.GreaterThanEqualsToken
}

case class EqualsEqualsEqualsToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.EqualsEqualsEqualsToken
}

case class ExclamationEqualsEqualsToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.ExclamationEqualsEqualsToken
}

case class PlusToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.PlusToken
}

case class MinusToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.MinusToken
}

case class AsteriskToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.AsteriskToken
}

case class SlashToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.SlashToken
}

case class PercentToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.PercentToken
}

case class AmpersandToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.AmpersandToken
}

case class BarToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.BarToken
}

case class PlusPlusToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.PlusPlusToken
}

case class MinusMinusToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.MinusMinusToken
}

case class LessThanLessThanToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.LessThanLessThanToken
}

case class GreaterThanGreaterThanToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.GreaterThanGreaterThanToken
}

case class GreaterThanGreaterThanGreaterThanToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.GreaterThanGreaterThanGreaterThanToken
}

case class ExclamationToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.ExclamationToken
}

case class AmpersandAmpersandToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.AmpersandAmpersandToken
}

case class BarBarToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.BarBarToken
}

case class QuestionToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.QuestionToken
}

case class ColonToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.ColonToken
}

case class EqualsToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.EqualsToken
}

case class PlusEqualsToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.PlusEqualsToken
}

case class MinusEqualsToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.MinusEqualsToken
}

case class SlashEqualsToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.SlashEqualsToken
}

case class GreaterThanGreaterThanGreaterThanEqualsToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.GreaterThanGreaterThanGreaterThanEqualsToken
}

case class CaretToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.CaretToken
}

case class AmpersandEqualsToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.AmpersandEqualsToken
}

case class BarEqualsToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.BarEqualsToken
}

case class CaretEqualsToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.CaretEqualsToken
}
