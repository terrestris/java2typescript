package java2typescript.transformer

import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.`type`.{ClassOrInterfaceType, Type}
import com.github.javaparser.ast.expr.{ArrayAccessExpr, ArrayCreationExpr, ArrayInitializerExpr, AssignExpr, BinaryExpr, CastExpr, ConditionalExpr, EnclosedExpr, Expression, FieldAccessExpr, InstanceOfExpr, LiteralExpr, MethodCallExpr, NameExpr, ObjectCreationExpr, SuperExpr, ThisExpr, UnaryExpr, VariableDeclarationExpr}
import java2typescript.analyseExports.Import
import java2typescript.ast
import java2typescript.ast.{ConditionalExpression, SyntaxKind}

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

def transformExpression(context: ParameterContext, expr: Expression): ast.Expression =
  expr match
    case expr: VariableDeclarationExpr => transformVariableDeclarationExpression(context, expr)
    case expr: LiteralExpr => transformLiteral(expr)
    case expr: ObjectCreationExpr => transformObjectCreationExpression(context, expr)
    case expr: BinaryExpr => transformBinaryExpression(context, expr)
    case expr: UnaryExpr => transformUnaryExpression(context, expr)
    case expr: EnclosedExpr => ast.ParenthesizedExpression(transformExpression(context, expr.getInner))
    case expr: NameExpr => transformNameInContext(context, expr.getName)
    case expr: AssignExpr => transformAssignExpression(context, expr)
    case expr: FieldAccessExpr => transformFieldAccessExpression(context, expr)
    case expr: ThisExpr => ast.ThisKeyword()
    case expr: MethodCallExpr => transformMethodCall(context, expr)
    case expr: ArrayCreationExpr => transformArrayCreationExpression(context, expr)
    case expr: ArrayAccessExpr => transformArrayAccessExpression(context, expr)
    case expr: CastExpr => transformCastExpression(context, expr)
    case expr: SuperExpr => ast.SuperKeyword()
    case expr: ConditionalExpr => transformConditionalExpression(context, expr)
    case expr: InstanceOfExpr => ast.BinaryExpression(
      transformExpression(context, expr.getExpression),
      transformName(expr.getType.asInstanceOf[ClassOrInterfaceType].getName),
      ast.InstanceOfKeyword()
    )
    case expr: ArrayInitializerExpr => ast.ArrayLiteralExpression(
      expr.getValues
        .asScala
        .map(transformExpression.curried(context))
        .toList
    )
    case _ => throw new Error("not supported")

def transformVariableDeclarationExpression(context: ParameterContext, expr: VariableDeclarationExpr) =
  ast.VariableDeclarationList(
    expr.getVariables.asScala.map(transformDeclaratorToVariable.curried(context)).toList
  )

def transformConditionalExpression(context: ParameterContext, expr: ConditionalExpr) =
  ast.ConditionalExpression(
    transformExpression(context, expr.getCondition),
    transformExpression(context, expr.getThenExpr),
    transformExpression(context, expr.getElseExpr)
  )

def transformCastExpression(context: ParameterContext, expr: CastExpr) =
  val `type` = transformType(context, expr.getType).get
  val castExpression = transformExpression(context, expr.getExpression)
  if (`type`.kind == SyntaxKind.NumberKeyword)
    ast.CallExpression(
      ast.PropertyAccessExpression(
        ast.Identifier("Math"),
        ast.Identifier("floor")
      ),
      List(castExpression)
    )
  else
    ast.AsExpression(
      castExpression,
      `type`
    )

def transformArrayAccessExpression(context: ParameterContext, expr: ArrayAccessExpr) =
  ast.ElementAccessExpression(
    transformExpression(context, expr.getName),
    transformExpression(context, expr.getIndex)
  )

def transformArrayCreationExpression(context: ParameterContext, expr: ArrayCreationExpr) =
  ast.ArrayLiteralExpression(
    expr.getInitializer.toScala.toList
      .flatMap(ie => ie.getValues.asScala)
      .map(transformExpression.curried(context))
  )

def transformMethodCall(context: ParameterContext, expr: MethodCallExpr): ast.Expression =
  val scopeExpr = expr.getScope.toScala
  val arguments = expr.getArguments.asScala.map(transformExpression.curried(context)).toList
  if (scopeExpr.isEmpty)
    ast.CallExpression(
      transformNameInContext(context, expr.getName),
      arguments
    )
  else
    if (scopeExpr.get.isNameExpr)
      val scope = Some(scopeExpr.get.asNameExpr.getName.asString)
      val name = expr.getName.asString
      context.addImportIfNeeded(scope, name)
      val im = context.getImport(scope, name)
      im match {
        case None => ast.CallExpression(
          ast.PropertyAccessExpression(
            transformExpression(context, scopeExpr.get),
            transformName(expr.getName)
          ),
          arguments
        )
        case imVal: Some[Import] => ast.CallExpression(
          ast.Identifier(imVal.get.javaName),
          arguments
        )
      }
    else
      ast.CallExpression(
        ast.PropertyAccessExpression(
          transformExpression(context, scopeExpr.get),
          transformName(expr.getName)
        ),
        arguments
      )


def transformBinaryExpression(context: ParameterContext, expr: BinaryExpr): ast.BinaryExpression =
  ast.BinaryExpression(
    transformExpression(context, expr.getLeft),
    transformExpression(context, expr.getRight),
    transformOperator(expr.getOperator.name)
  )

def transformUnaryExpression(context: ParameterContext, expr: UnaryExpr): ast.PrefixUnaryExpression|ast.PostfixUnaryExpression =
  if (expr.getOperator.isPrefix)
    ast.PrefixUnaryExpression(
      transformOperator(expr.getOperator.name).kind,
      transformExpression(context, expr.getExpression)
    )
  else
    ast.PostfixUnaryExpression(
      transformOperator(expr.getOperator.name).kind,
      transformExpression(context, expr.getExpression)
    )

def transformObjectCreationExpression(context: ParameterContext, expr: ObjectCreationExpr) =
  context.addImportIfNeeded(getTypeScope(expr.getType), getTypeName(expr.getType))
  ast.NewExpression(
    ast.Identifier(expr.getType.getName.getIdentifier),
    transformArguments(context, expr.getArguments),
    transformTypeArguments(context, expr.getTypeArguments)
  )

def transformArguments(context: ParameterContext, expressions: NodeList[Expression]) =
  expressions.asScala.map(transformExpression.curried(context)).toList

def transformFieldAccessExpression(context: ParameterContext, expr: FieldAccessExpr) =
  if (expr.getScope.isNameExpr)
    val name = expr.getScope.asNameExpr.getName
    context.addImportIfNeeded(None, name.asString)
  ast.PropertyAccessExpression(
    transformExpression(context, expr.getScope),
    transformName(expr.getName)
  )

def transformAssignExpression(context: ParameterContext, expr: AssignExpr) =
  ast.BinaryExpression(
    transformExpression(context, expr.getTarget),
    transformExpression(context, expr.getValue),
    if (expr.getOperator.name == "ASSIGN")
      transformOperator("ASSIGN")
    else
      transformOperator(s"${expr.getOperator.name}_EQUALS")
  )
