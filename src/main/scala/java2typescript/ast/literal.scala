package java2typescript.ast

trait Literal extends Expression

case class NumericLiteral(text: String) extends Literal {
  val kind: SyntaxKind = SyntaxKind.NumericLiteral
}

case class StringLiteral(
  text: String
) extends Literal {
  val kind: SyntaxKind = SyntaxKind.StringLiteral
  val hasExtendedUnicodeEscape = false
}