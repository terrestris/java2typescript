package java2typescript.transformer

import com.github.javaparser.ast.stmt.{AssertStmt, BlockStmt, BreakStmt, CatchClause, ContinueStmt, DoStmt, EmptyStmt, ExplicitConstructorInvocationStmt, ExpressionStmt, ForEachStmt, ForStmt, IfStmt, ReturnStmt, Statement, SwitchStmt, ThrowStmt, TryStmt, WhileStmt}
import java2typescript.ast

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

def transformBlockStatement(context: ParameterContext, stmt: BlockStmt): ast.Block =
  ast.Block(stmt.getStatements.asScala.map(transformStatement.curried(context)).toList)

def transformSwitchStatement(context: ParameterContext, stmt: SwitchStmt) =
  ast.SwitchStatement(
    transformExpression(context, stmt.getSelector),
    ast.CaseBlock(
      stmt.getEntries.asScala.toList.map(entry =>
        val labels = entry.getLabels.asScala
        val statements = entry.getStatements.asScala.map(transformStatement.curried(context)).toList
        if (labels.nonEmpty)
          if (labels.length > 1)
            throw new Error("more than one label not supported for switch statement")
          ast.CaseClause(
            transformExpression(context, labels.head),
            statements
          )
        else
          ast.DefaultClause(statements)
      )
    )
  )

def transformStatement(context: ParameterContext, stmt: Statement): ast.Statement =
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
    case stmt: SwitchStmt => transformSwitchStatement(context, stmt)
    case stmt: ForEachStmt => ast.ForOfStatement(
      transformVariableDeclarationExpression(context, stmt.getVariable),
      transformExpression(context, stmt.getIterable),
      transformStatement(context, stmt.getBody)
    )
    case stmt: DoStmt => ast.DoStatement(
      transformStatement(context, stmt.getBody),
      transformExpression(context, stmt.getCondition)
    )
    case stmt: EmptyStmt => ast.EmptyStatement()
    case stmt: AssertStmt => transformAssertStatement(context, stmt)
    case _ => throw new Error("statement type not supported")

def transformAssertStatement(context: ParameterContext, stmt: AssertStmt) =
  ast.IfStatement(
    ast.PrefixUnaryExpression(
      ast.SyntaxKind.ExclamationToken,
      ast.ParenthesizedExpression(transformExpression(context, stmt.getCheck)),
    ),
    ast.ThrowStatement(
      ast.NewExpression(
        ast.Identifier("Error"),
        List(
          ast.StringLiteral(stmt.getMessage.map { m => m.asStringLiteralExpr().asString() }.orElse("Assertion failed"))
        )
      )
    )
  )

def transformCatchClauses(context: ParameterContext, clauses: List[CatchClause]): Option[ast.CatchClause] =
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
          Some(ast.AnyKeyword())
        ),
        transformBlockStatement(context, clauses.head.getBody)
      )
    )


def transformTryStatement(context: ParameterContext, stmt: TryStmt) =
  ast.TryStatement(
    tryBlock = transformBlockStatement(context, stmt.getTryBlock),
    catchClause = transformCatchClauses(context, stmt.getCatchClauses.asScala.toList),
    finallyBlock = stmt.getFinallyBlock.toScala.map(transformBlockStatement.curried(context))
  )

def transformThrowStatement(context: ParameterContext, stmt: ThrowStmt): ast.ThrowStatement =
  ast.ThrowStatement(
    transformExpression(context, stmt.getExpression)
  )

def transformForStatment(context: ParameterContext, stmt: ForStmt) =
  val init = stmt.getInitialization.asScala
  if (init.length > 1)
    throw new Error("only one initializer for for loop supported")
  val transformedInit =
    if (init.nonEmpty)
      if (init.length > 1)
        throw new Error("only one expression supported in initializer")
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

def transformIfStatement(context: ParameterContext, stmt: IfStmt) =
  ast.IfStatement(
    transformExpression(context, stmt.getCondition),
    transformStatement(context, stmt.getThenStmt),
    stmt.getElseStmt.toScala.map(transformStatement.curried(context))
  )
