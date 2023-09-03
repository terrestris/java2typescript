package de.terrestris.java2typescript.transformer

import com.github.javaparser.ast.expr.VariableDeclarationExpr
import com.github.javaparser.ast.stmt.{BlockStmt, BreakStmt, ContinueStmt, ExpressionStmt, ForStmt, IfStmt, ReturnStmt, Statement, ThrowStmt, WhileStmt}
import de.terrestris.java2typescript.ast

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

def transformBlockStatement(context: Context, stmt: BlockStmt): ast.Block =
  ast.Block(stmt.getStatements.asScala.map(transformStatement.curried(context)).toList)

def transformStatement(context: Context, stmt: Statement): ast.Statement =
  stmt match
    case stmt: ExpressionStmt =>
      val expr = transformExpression(context, stmt.getExpression)
      expr match
        case expr: ast.VariableDeclarationList => ast.VariableStatement(expr)
        case expr => ast.ExpressionStatement(expr)
    //    case stmt: LocalClassDeclarationStmt => transformClassOrInterfaceDeclaration(stmt.getClassDeclaration)
    case stmt: ReturnStmt => ast.ReturnStatement(stmt.getExpression.toScala.map(transformExpression.curried(context)))
    case stmt: IfStmt => transformIfStatement(context, stmt)
    case stmt: BlockStmt => transformBlockStatement(context, stmt)
    case stmt: ThrowStmt => ast.ThrowStatement(transformExpression(context, stmt.getExpression))
    case stmt: WhileStmt => ast.WhileStatement(
      transformExpression(context, stmt.getCondition),
      transformStatement(context, stmt.getBody)
    )
    case stmt: ForStmt => transformForStatment(context, stmt)
    case stmt: BreakStmt => ast.BreakStatement()
    case stmt: ContinueStmt => ast.ContinueStatement()
    case _ => throw new Error("statement type not supported")

def transformForStatment(context: Context, stmt: ForStmt) = {
  val init = stmt.getInitialization.asScala
  if (init.length > 1)
    throw new Error("only one initializer for for loop supported")
  val transformedInit =
    if (init.nonEmpty)
      if (!init.head.isInstanceOf[VariableDeclarationExpr])
        throw new Error("only variable declaration expressions are supported in initializer")
      else
        Some(transformExpression(context, init.head))
    else
      None
  val update = stmt.getUpdate.asScala
  if (update.length > 1)
    throw new Error("only one updater for for loop supported")
  val transformedUpdate =
    if (update.nonEmpty)
      Some(transformExpression(context, update.head))
    else
      None
  ast.ForStatement(
    initializer = transformedInit,
    condition = stmt.getCompare.toScala.map(transformExpression.curried(context)),
    incrementor = transformedUpdate,
    statement = transformStatement(context, stmt.getBody)
  )
}

def transformIfStatement(context: Context, stmt: IfStmt) =
  ast.IfStatement(
    transformExpression(context, stmt.getCondition),
    transformStatement(context, stmt.getThenStmt),
    stmt.getElseStmt.toScala.map(transformStatement.curried(context))
  )
