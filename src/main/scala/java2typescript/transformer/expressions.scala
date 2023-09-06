package de.terrestris.java2typescript.transformer

import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.`type`.{ClassOrInterfaceType, Type}
import com.github.javaparser.ast.expr.{ArrayAccessExpr, ArrayCreationExpr, ArrayInitializerExpr, AssignExpr, BinaryExpr, CastExpr, ConditionalExpr, EnclosedExpr, Expression, FieldAccessExpr, InstanceOfExpr, LiteralExpr, MethodCallExpr, NameExpr, ObjectCreationExpr, SuperExpr, ThisExpr, UnaryExpr, VariableDeclarationExpr}
import de.terrestris.java2typescript.ast
import de.terrestris.java2typescript.ast.{ConditionalExpression, SyntaxKind}

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

def transformExpression(context: Context, expr: Expression): ast.Expression =
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

def transformVariableDeclarationExpression(context: Context, expr: VariableDeclarationExpr) =
  ast.VariableDeclarationList(
    expr.getVariables.asScala.map(transformDeclaratorToVariable.curried(context)).toList
  )

def transformConditionalExpression(context: Context, expr: ConditionalExpr) =
  ast.ConditionalExpression(
    transformExpression(context, expr.getCondition),
    transformExpression(context, expr.getThenExpr),
    transformExpression(context, expr.getElseExpr)
  )

def transformCastExpression(context: Context, expr: CastExpr) =
  val `type` = transformType(expr.getType)
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

def transformArrayAccessExpression(context: Context, expr: ArrayAccessExpr) =
  ast.ElementAccessExpression(
    transformExpression(context, expr.getName),
    transformExpression(context, expr.getIndex)
  )

def transformArrayCreationExpression(context: Context, expr: ArrayCreationExpr) =
  ast.ArrayLiteralExpression(
    expr.getInitializer.toScala.toList
      .flatMap(ie => ie.getValues.asScala)
      .map(transformExpression.curried(context))
  )

def transformMethodCall(context: Context, expr: MethodCallExpr) =
  val scope = expr.getScope.toScala
  val arguments = expr.getArguments.asScala.map(transformExpression.curried(context)).toList
  if (scope.isEmpty)
    ast.CallExpression(
      transformNameInContext(context, expr.getName),
      arguments
    )
  else
    ast.CallExpression(
      ast.PropertyAccessExpression(
        transformExpression(context, scope.get),
        transformName(expr.getName)
      ),
      arguments
    )

def transformBinaryExpression(context: Context, expr: BinaryExpr): ast.BinaryExpression =
  ast.BinaryExpression(
    transformExpression(context, expr.getLeft),
    transformExpression(context, expr.getRight),
    transformOperator(expr.getOperator.name)
  )

def transformUnaryExpression(context: Context, expr: UnaryExpr): ast.PrefixUnaryExpression|ast.PostfixUnaryExpression =
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

def transformObjectCreationExpression(context: Context, expr: ObjectCreationExpr) =
  ast.NewExpression(
    ast.Identifier(expr.getType.getName.getIdentifier),
    transformArguments(context, expr.getArguments),
    transformTypeArguments(expr.getTypeArguments)
  )

def transformArguments(context: Context, expressions: NodeList[Expression]) =
  expressions.asScala.map(transformExpression.curried(context)).toList

def transformFieldAccessExpression(context: Context, expr: FieldAccessExpr) =
  ast.PropertyAccessExpression(
    transformExpression(context, expr.getScope),
    transformName(expr.getName)
  )

def transformAssignExpression(context: Context, expr: AssignExpr) =
  ast.BinaryExpression(
    transformExpression(context, expr.getTarget),
    transformExpression(context, expr.getValue),
    if (expr.getOperator.name == "ASSIGN")
      transformOperator("ASSIGN")
    else
      transformOperator(s"${expr.getOperator.name}_EQUALS")
  )
