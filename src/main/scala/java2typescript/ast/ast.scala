package de.terrestris.java2typescript.ast

trait Node {
  val kind: Int
  val `flags`: Int = 0
}

trait Expression extends Node

trait Statement extends Node

trait Keyword extends Node

trait Token extends Node

type Type = Keyword|TypeReference

type Modifier = ExportKeyword

class Identifier(val escapedText: String) extends Node {
  val kind = 80
}

class VariableDeclaration(
  val name: Identifier,
  //  exclamationToken: Null,
  val `type`: Type,
  val initializer: Option[Expression]
) extends Node {
  val kind = 259
}

class TypeReference(
  val typeName: Identifier,
  val typeArguments: List[Type] = List()
) extends Node {
  val kind = 182
}

class ClassDeclaration(
  val name: Identifier,
  val typeParameters: List[Type] = List(),
  val heritageClauses: List[Type] = List(),
  val members: List[Object] = List(),
  val modifiers: List[Modifier] = List()
) extends Node {
  val kind = 262
}

class InterfaceDeclaration(
  val name: Identifier,
  val typeParameters: List[Type] = List(),
  val heritageClauses: List[Type] = List(),
  val members: List[Object] = List(),
  val modifiers: List[Modifier] = List()
) extends Node {
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

class ExportKeyword() extends Node {
  val kind = 95
}

// Expression

trait Literal extends Expression

class VariableDeclarationList(
  val declarations: List[VariableDeclaration],
  override val flags: Int = 1 // 1 = Let
) extends Expression {
  val kind = 260
}

class NewExpression (
  val expression: Identifier,
  val arguments: List[Expression] = List(),
  val typeArguments: List[Keyword|TypeReference] = List()
) extends Expression {
  val kind = 213
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

class ParenthesizedExpression(
  val expression: Expression
) extends Expression {
  val kind = 216
}

// Literal

class NumericLiteral(val text: String) extends Literal {
  val kind = 9
}

class StringLiteral(
  val text: String,
  val hasExtendedUnicodeEscape: Boolean = false
) extends Literal {
  val kind = 11
}

// Statement

class VariableStatement(val declarationList: VariableDeclarationList) extends Statement {
  val kind = 242
}

// Keyword

class StringKeyword() extends Keyword {
  val kind = 154
}

class NumberKeyword() extends Keyword {
  val kind = 150
}

// Token

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
