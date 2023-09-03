package de.terrestris.java2typescript.transformer

import com.github.javaparser.ast.expr.{ObjectCreationExpr, VariableDeclarationExpr}
import com.github.javaparser.ast.stmt.{BlockStmt, BreakStmt, CatchClause, ContinueStmt, ExplicitConstructorInvocationStmt, ExpressionStmt, ForStmt, IfStmt, ReturnStmt, Statement, ThrowStmt, TryStmt, WhileStmt}
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
    case stmt: ThrowStmt => transformThrowStatement(context, stmt)
    case stmt: WhileStmt => ast.WhileStatement(
      transformExpression(context, stmt.getCondition),
      transformStatement(context, stmt.getBody)
    )
    case stmt: ForStmt => transformForStatment(context, stmt)
    case stmt: BreakStmt => ast.BreakStatement()
    case stmt: ContinueStmt => ast.ContinueStatement()
    case stmt: TryStmt => transformTryStatement(context, stmt)
    case stmt: ExplicitConstructorInvocationStmt => ast.ExpressionStatement(
      ast.CallExpression(
        ast.SuperKeyword()
      )
    )
    case _ => throw new Error("statement type not supported")

def transformCatchClauses(context: Context, clauses: List[CatchClause]): Option[ast.CatchClause] =
  if clauses.isEmpty then
    None
  else if clauses.length > 1 then
    throw new Error("multiple catch branches not supported")
    // TODO: This will be tricky as error types are probably are not available in typescript code.
    //  Also the errors might not be thrown at all
  else
    Some(
      ast.CatchClause(
        ast.VariableDeclaration(
          ast.Identifier(clauses.head.getParameter.getName.asString),
          ast.AnyKeyword()
        ),
        transformBlockStatement(context, clauses.head.getBody)
      )
    )


def transformTryStatement(context: Context, stmt: TryStmt) =
  ast.TryStatement(
    tryBlock = transformBlockStatement(context, stmt.getTryBlock),
    catchClause = transformCatchClauses(context, stmt.getCatchClauses.asScala.toList),
    finallyBlock = stmt.getFinallyBlock.toScala.map(transformBlockStatement.curried(context))
  )

def transformThrowStatement(context: Context, stmt: ThrowStmt): ast.ThrowStatement =
  stmt.getExpression match
    case err: ObjectCreationExpr =>
      val name = err.getType.getName
      if (name.asString() == "Error")
        return ast.ThrowStatement(transformExpression(context, err))
      val args = err.getArguments.asScala
      if (args.length != 1)
        throw new Error("more then one error argument not supported")
      val arg = args.head
      if (!arg.isStringLiteralExpr)
        throw new Error("Non string arguments for errors are not supported")
      val text = arg.toStringLiteralExpr.get.asString

      ast.ThrowStatement(
        ast.NewExpression(
          ast.Identifier("Error"),
          List(ast.StringLiteral(s"$name: $text")
        )
      )
    )
    case _ => throw new Error("throw type not supported")

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
