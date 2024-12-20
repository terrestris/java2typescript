package java2typescript.transformer

import com.github.javaparser.ast.expr.{ObjectCreationExpr, SimpleName}
import java2typescript.ast

import scala.jdk.CollectionConverters.*

def transformObjectCreationExpression(context: ParameterContext, expr: ObjectCreationExpr): ast.Expression =
  if (isExceptionType(expr))
    transformExceptionCreationExpression(context, expr)
  else if (isArrayType(expr))
    transformArrayExpression(context, expr)
  else
    context.addImportIfNeeded(getTypeScope(expr.getType), getTypeName(expr.getType))
    ast.NewExpression(
      ast.Identifier(expr.getType.getName.getIdentifier),
      transformArguments(context, expr.getArguments),
      transformTypeArguments(context, expr.getTypeArguments)
    )

def isExceptionType(expr: ObjectCreationExpr): Boolean =
  getTypeName(expr.getType).endsWith("Exception") || getTypeName(expr.getType).endsWith("Error")

def transformExceptionCreationExpression(context: ParameterContext, expr: ObjectCreationExpr): ast.Expression =
  val name = expr.getType.getName
  if (name.asString() == "Error")
    return ast.NewExpression(
      ast.Identifier("Error"),
      transformArguments(context, expr.getArguments),
      transformTypeArguments(context, expr.getTypeArguments)
    )
  val args = expr.getArguments.asScala
  if (args.length > 1)
    println(s"WARN: ${expr.getType.getName}: all but the first error argument are dropped.")

  ast.NewExpression(
    ast.Identifier("Error"),
    List(
      if (args.isEmpty)
        ast.StringLiteral(name.toString)
      else
        ast.BinaryExpression(
          ast.StringLiteral(s"$name: "),
          transformExpression(context, args.head),
          ast.PlusToken()
        )
    )
  )

def isArrayType(expr: ObjectCreationExpr): Boolean =
  val typeName = getTypeName(expr.getType)
  typeName.endsWith("List") || typeName == "Collection" 

def transformArrayExpression(context: ParameterContext, expr: ObjectCreationExpr) =
  ast.ArrayLiteralExpression(List())

def isBuiltInType(name: SimpleName): Boolean =
  List("Math", "System").contains(name.asString)

def isDroppableInterface(name: SimpleName): Boolean =
  List("Cloneable", "Comparable", "Serializable").contains(name.asString)