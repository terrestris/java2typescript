package de.terrestris
package java2typescript.ast

trait Statement extends Node

class VariableStatement(val declarationList: VariableDeclarationList, val kind: Int = 242) extends Statement
