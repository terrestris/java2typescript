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

class Identifier(val escapedText: String) extends Expression {
  val kind = 80
}

class Parameter(
  val name: Identifier,
  val `type`: Type,
  val modifiers: List[Modifier] = List()
) extends Node {
  val kind = 168
}

class TypeReference(
  val typeName: Identifier,
  val typeArguments: List[Type] = List()
) extends Type {
  val kind = 182
}

class ClassDeclaration(
  val name: Identifier,
  val typeParameters: List[Type] = List(),
  val heritageClauses: List[Type] = List(),
  val members: List[Member] = List(),
  val modifiers: List[Modifier] = List()
) extends Statement {
  val kind = 262
}

class VariableDeclaration(
  val name: Identifier,
  val `type`: Type,
  val initializer: Option[Expression]
) extends Node {
  val kind = 259
}

class InterfaceDeclaration(
  val name: Identifier,
  val typeParameters: List[Type] = List(),
  val heritageClauses: List[Type] = List(),
  val members: List[Member] = List(),
  val modifiers: List[Modifier] = List()
) extends Statement {
  val kind = 263
}

class ImportDeclaration(
  val importClause: ImportClause,
  val moduleSpecifier: StringLiteral
) extends Node {
  val kind = 271
}

class ImportClause(
  val namedBindings: NamedImports
) extends Node {
  val kind = 272
  val isTypeOnly = false
}

class NamedImports(
  val elements: List[ImportSpecifier]
) extends Node {
  val kind = 274
}

class ImportSpecifier(
  val name: Identifier
) extends Node {
  val kind = 275
  val isTypeOnly = false
}

class ArrayType(
  val elementType: Type
) extends Type {
  val kind = 187
}

// Members

class PropertyDeclaration(
  val name: Identifier,
  val `type`: Type,
  val initializer: Option[Expression],
  val modifiers: List[Modifier] = List()
) extends Member {
  val kind = 171
}

class MethodDeclaration(
  val name: Identifier,
  val `type`: Type,
  val parameters: List[Parameter] = List(),
  val typeParameters: List[Type] = List(),
  val body: Option[Block] = None,
  val modifiers: List[Modifier] = List(),
) extends Member {
  val kind = 173
}

class Constructor(
  val parameters: List[Parameter] = List(),
  val body: Option[Block] = None,
  val modifiers: List[Modifier] = List(),
) extends Member {
  val kind = 175
}

// Expression

trait Literal extends Expression

class ArrayLiteralExpression(
  val elements: List[Expression]
) extends Expression {
  val kind = 208
}

class PropertyAccessExpression(
  val expression: Expression,
  val name: Identifier
) extends Expression {
  val kind = 210
}

class ElementAccessExpression(
  val expression: Expression,
  val argumentExpression: Expression
) extends Expression {
  val kind = 211
}

class CallExpression(
  val expression: Expression,
  val arguments: List[Expression] = List(),
  val typeArguments: List[Type] = List()
) extends Expression {
  val kind = 212
}

class NewExpression (
  val expression: Identifier,
  val arguments: List[Expression] = List(),
  val typeArguments: List[Type] = List()
) extends Expression {
  val kind = 213
}

class ParenthesizedExpression(
  val expression: Expression
) extends Expression {
  val kind = 216
}

class BinaryExpression(
  val left: Expression,
  val right: Expression,
  val operatorToken: Token
) extends Expression {
  val kind = 225
}

class PrefixUnaryExpression(
  val operator: Int,
  val operand: Expression
) extends Expression {
  val kind = 223
}

class VariableDeclarationList(
  val declarations: List[VariableDeclaration],
  override val flags: Int = 1 // 1 = Let
) extends Expression {
  val kind = 260
}

// Literal

class NumericLiteral(val text: String) extends Literal {
  val kind = 9
}

class StringLiteral(
  val text: String
) extends Literal {
  val kind = 11
  val hasExtendedUnicodeEscape = false
}

// Statement

class Block(
  val statements: List[Statement]
) extends Statement {
  val kind = 240
}

class VariableStatement(
  val declarationList: VariableDeclarationList
) extends Statement {
  val kind = 242
}

class ExpressionStatement(
  val expression: Expression
) extends Statement {
  val kind = 243
}

class IfStatement(
  val expression: Expression,
  val thenStatement: Statement,
  val elseStatement: Option[Statement]
) extends Statement {
  val kind = 244
}

class ReturnStatement(
  val expression: Option[Expression]
) extends Statement {
  val kind = 252
}

// Keyword

class ExportKeyword() extends Modifier {
  val kind = 95
}

class FalseKeyword() extends Literal {
  val kind = 97
}

class ThisKeyword() extends Expression {
  val kind = 110
}

class TrueKeyword() extends Literal {
  val kind = 112
}

class VoidKeyword() extends Type {
  val kind = 116
}

class PrivateKeyword() extends Modifier {
  val kind = 123
}

class ProtectedKeyword() extends Modifier {
  val kind = 124
}

class PublicKeyword() extends Modifier {
  val kind = 125
}

class BooleanKeyword() extends Type {
  val kind = 136
}

class NumberKeyword() extends Type {
  val kind = 150
}

class StringKeyword() extends Type {
  val kind = 154
}

// Token

class LessThanToken() extends Token {
  val kind = 30
}

class GreaterThanToken() extends Token {
  val kind = 32
}

class LessThanEqualsToken() extends Token {
  val kind = 33
}

class GreaterThanEqualsToken() extends Token {
  val kind = 34
}

class EqualsEqualsEqualsToken() extends Token {
  val kind = 37
}

class ExclamationEqualsEqualsToken() extends Token {
  val kind = 38
}

class PlusToken() extends Token {
  val kind = 40
}

class MinusToken() extends Token {
  val kind = 41
}

class SlashToken() extends Token {
  val kind = 44
}

class AsteriskToken() extends Token {
  val kind = 42
}

class AmpersandAmpersandToken() extends Token {
  val kind = 56
}

class BarBarToken() extends Token {
  val kind = 57
}

class EqualsToken() extends Token {
  val kind = 64
}
