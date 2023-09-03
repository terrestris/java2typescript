package de.terrestris.java2typescript.transformer

import de.terrestris.java2typescript.ast

def transformOperator(name: String): ast.Token =
  name match
    case "ASSIGN" => ast.EqualsToken()
    case "PLUS" => ast.PlusToken()
    case "MINUS" => ast.MinusToken()
    case "MULTIPLY" => ast.AsteriskToken()
    case "DIVIDE" => ast.SlashToken()
    case "AND" => ast.AmpersandAmpersandToken()
    case "OR" => ast.BarBarToken()
    case "EQUALS" => ast.EqualsEqualsEqualsToken()
    case "PLUS_EQUALS" => ast.PlusEqualsToken()
    case "MINUS_EQUALS" => ast.MinusEqualsToken()
    case "NOT_EQUALS" => ast.ExclamationEqualsEqualsToken()
    case "LESS_EQUALS" => ast.LessThanEqualsToken()
    case "LESS" => ast.LessThanToken()
    case "GREATER" => ast.GreaterThanToken()
    case "GREATER_EQUALS" => ast.GreaterThanEqualsToken()
    case "LOGICAL_COMPLEMENT" => ast.ExclamationToken()
    case "POSTFIX_INCREMENT"|"PREFIX_INCREMENT" => ast.PlusPlusToken()
    case "POSTFIX_DECREMENT"|"PREFIX_DECREMENT" => ast.MinusMinusToken()
    case _ => throw new Error("not supported")
