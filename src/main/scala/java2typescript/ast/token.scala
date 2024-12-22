package java2typescript.ast

trait Token extends Node

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

case class TildeToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.TildeToken
}

case class AsteriskEqualsToken() extends Token {
  val kind: SyntaxKind = SyntaxKind.AsteriskEqualsToken
}