package java2typescript.transformer

import com.github.javaparser.ast.`type`.ClassOrInterfaceType
import com.github.javaparser.ast.body.{BodyDeclaration, MethodDeclaration}
import com.github.javaparser.ast.expr.{ObjectCreationExpr, SimpleName}
import java2typescript.ast

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

def transformObjectCreationExpression(context: ParameterContext, expr: ObjectCreationExpr): ast.Expression =
  if (isExceptionType(expr))
    transformExceptionCreationExpression(context, expr)
  else if (isArrayType(expr))
    transformArrayExpression(context, expr)
  else
    context.addImportIfNeeded(getTypeScope(expr.getType), getTypeName(expr.getType))
    val constructable = expr.getAnonymousClassBody.toScala match {
      case Some(anonClass) => transformAnonymousClass(context, expr.getType, anonClass.asScala.toList)
      case None => ast.Identifier(expr.getType.getName.getIdentifier)
    }

    ast.NewExpression(
      constructable,
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
  List("Cloneable", "Serializable").contains(name.asString)

def transformAnonymousClass(context: ClassContext, classType: ClassOrInterfaceType, body: List[BodyDeclaration[_]]) =
  if (body.isEmpty) {
    throw new Error("Anonymous classes with no body declarations are not supported")
  }

  val anonClassContext = ClassContext(context.fileContext, None, Some(context))

  val members = body.flatMap(m => transformMember(anonClassContext, m))

  ast.ParenthesizedExpression(ast.ClassExpression(
    None,
    members = members,
    modifiers = List(),
    heritageClauses = transformHeritage(anonClassContext, List(classType), ast.SyntaxKind.ExtendsKeyword).toList
  ))
