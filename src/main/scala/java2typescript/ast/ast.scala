package de.terrestris.java2typescript.ast

trait Node {
  val kind: Int
  val `flags`: Int = 0
}

trait Expression extends Node

trait Statement extends Node

trait Token extends Node

trait Modifier extends Node

trait Member extends Node

trait Type extends Node

case class Identifier(escapedText: String) extends Expression {
  val kind = 80
}

case class Parameter(
  name: Identifier,
  `type`: Type,
  modifiers: List[Modifier] = List(),
  dotDotDotToken: Option[DotDotDotToken] = None
) extends Node {
  val kind = 168
}

case class TypeReference(
  typeName: Identifier,
  typeArguments: List[Type] = List()
) extends Type {
  val kind = 182
}

case class ClassDeclaration(
  name: Identifier,
  typeParameters: List[Type] = List(),
  heritageClauses: List[Type] = List(),
  members: List[Member] = List(),
  modifiers: List[Modifier] = List()
) extends Statement {
  val kind = 262
}

case class VariableDeclaration(
  name: Identifier,
  `type`: Type,
  initializer: Option[Expression]
) extends Node {
  val kind = 259
}

case class InterfaceDeclaration(
  name: Identifier,
  typeParameters: List[Type] = List(),
  heritageClauses: List[Type] = List(),
  members: List[Member] = List(),
  modifiers: List[Modifier] = List()
) extends Statement {
  val kind = 263
}

case class ImportDeclaration(
  importClause: ImportClause,
  moduleSpecifier: StringLiteral
) extends Node {
  val kind = 271
}

case class ImportClause(
  namedBindings: NamedImports
) extends Node {
  val kind = 272
  val isTypeOnly = false
}

case class NamedImports(
  elements: List[ImportSpecifier]
) extends Node {
  val kind = 274
}

case class ImportSpecifier(
  name: Identifier
) extends Node {
  val kind = 275
  val isTypeOnly = false
}

case class ArrayType(
  elementType: Type
) extends Type {
  val kind = 187
}

// Members

case class PropertyDeclaration(
  name: Identifier,
  `type`: Type,
  initializer: Option[Expression],
  modifiers: List[Modifier] = List()
) extends Member {
  val kind = 171
}

case class MethodDeclaration(
  name: Identifier,
  `type`: Type,
  parameters: List[Parameter] = List(),
  typeParameters: List[Type] = List(),
  body: Option[Block] = None,
  modifiers: List[Modifier] = List(),
) extends Member {
  val kind = 173
}

case class Constructor(
  parameters: List[Parameter] = List(),
  body: Option[Block] = None,
  modifiers: List[Modifier] = List(),
) extends Member {
  val kind = 175
}

// Expression

trait Literal extends Expression

case class ArrayLiteralExpression(
  elements: List[Expression]
) extends Expression {
  val kind = 208
}

case class PropertyAccessExpression(
  expression: Expression,
  name: Identifier
) extends Expression {
  val kind = 210
}

case class ElementAccessExpression(
  expression: Expression,
  argumentExpression: Expression
) extends Expression {
  val kind = 211
}

case class CallExpression(
  expression: Expression,
  arguments: List[Expression] = List(),
  typeArguments: List[Type] = List()
) extends Expression {
  val kind = 212
}

case class NewExpression (
  expression: Identifier,
  arguments: List[Expression] = List(),
  typeArguments: List[Type] = List()
) extends Expression {
  val kind = 213
}

case class ParenthesizedExpression(
  expression: Expression
) extends Expression {
  val kind = 216
}

case class TypeOfExpression(
  expression: Expression
) extends Expression {
  val kind = 220
}

case class BinaryExpression(
  left: Expression,
  right: Expression,
  operatorToken: Token
) extends Expression {
  val kind = 225
}

case class PrefixUnaryExpression(
  operator: Int,
  operand: Expression
) extends Expression {
  val kind = 223
}

case class VariableDeclarationList(
  declarations: List[VariableDeclaration],
  override val flags: Int = 1 // 1 = Let
) extends Expression {
  val kind = 260
}

// Literal

case class NumericLiteral(text: String) extends Literal {
  val kind = 9
}

case class StringLiteral(
  text: String
) extends Literal {
  val kind = 11
  val hasExtendedUnicodeEscape = false
}

// Statement

case class Block(
  statements: List[Statement]
) extends Statement {
  val kind = 240
}

case class VariableStatement(
  declarationList: VariableDeclarationList
) extends Statement {
  val kind = 242
}

case class ExpressionStatement(
  expression: Expression
) extends Statement {
  val kind = 243
}

case class IfStatement(
  expression: Expression,
  thenStatement: Statement,
  elseStatement: Option[Statement] = None
) extends Statement {
  val kind = 244
}

case class ReturnStatement(
  expression: Option[Expression]
) extends Statement {
  val kind = 252
}

case class ThrowStatement(
  expression: Expression
) extends Statement {
  val kind = 256
}

// Keyword

case class ExportKeyword() extends Modifier {
  val kind = 95
}

case class FalseKeyword() extends Literal {
  val kind = 97
}

case class InstanceOfKeyword() extends Token {
  val kind = 104
}

case class NullKeyword() extends Literal {
  val kind = 106
}

case class ThisKeyword() extends Expression {
  val kind = 110
}

case class TrueKeyword() extends Literal {
  val kind = 112
}

case class VoidKeyword() extends Type {
  val kind = 116
}

case class PrivateKeyword() extends Modifier {
  val kind = 123
}

case class ProtectedKeyword() extends Modifier {
  val kind = 124
}

case class PublicKeyword() extends Modifier {
  val kind = 125
}

case class StaticKeyword() extends Modifier {
  val kind = 126
}

case class AnyKeyword() extends Type {
  val kind = 133
}

case class BooleanKeyword() extends Type {
  val kind = 136
}

case class NumberKeyword() extends Type {
  val kind = 150
}

case class StringKeyword() extends Type {
  val kind = 154
}

// Token

case class DotDotDotToken() extends Token {
  val kind = 26
}

case class LessThanToken() extends Token {
  val kind = 30
}

case class GreaterThanToken() extends Token {
  val kind = 32
}

case class LessThanEqualsToken() extends Token {
  val kind = 33
}

case class GreaterThanEqualsToken() extends Token {
  val kind = 34
}

case class EqualsEqualsEqualsToken() extends Token {
  val kind = 37
}

case class ExclamationEqualsEqualsToken() extends Token {
  val kind = 38
}

case class PlusToken() extends Token {
  val kind = 40
}

case class MinusToken() extends Token {
  val kind = 41
}

case class SlashToken() extends Token {
  val kind = 44
}

case class AsteriskToken() extends Token {
  val kind = 42
}

case class AmpersandAmpersandToken() extends Token {
  val kind = 56
}

case class BarBarToken() extends Token {
  val kind = 57
}

case class EqualsToken() extends Token {
  val kind = 64
}
