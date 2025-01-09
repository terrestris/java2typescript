package java2typescript.ast

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

