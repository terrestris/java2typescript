package java2typescript.transformer

import com.github.javaparser.ast.body.{ConstructorDeclaration, MethodDeclaration, Parameter}
import com.github.javaparser.ast.expr.SimpleName
import java2typescript.ast

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

def transformConstructorDeclaration(context: ClassContext, declaration: ConstructorDeclaration) =
  val methodParameters = declaration.getParameters.asScala.map(transformParameter.curried(context)).toList
  val methodContext = ParameterContext(context, methodParameters.toBuffer)
  val methodBody = ast.Block(
    declaration.getBody.getStatements.asScala.map(transformStatement.curried(methodContext)).toList
  )
  val methodModifiers = declaration.getModifiers.asScala.flatMap(transformModifier).toList

  ast.Constructor(
    parameters = methodParameters,
    body = Some(methodBody),
    modifiers = methodModifiers
  )

def transformMethodDeclaration(context: ClassContext, decl: MethodDeclaration): ast.MethodDeclaration =
  if (decl.getName.asString == "clone")
    return createStructuredCloneMethod(context.classOrInterface.get.getName)

  val methodParameters = decl.getParameters.asScala.map(transformParameter.curried(context)).toList
  val methodContext = ParameterContext(context, methodParameters.toBuffer)
  val methodBody = decl.getBody.toScala.map(body =>
    ast.Block(body.getStatements.asScala.map(transformStatement.curried(methodContext)).toList)
  )
  val originalMethodModifiers = decl.getModifiers.asScala.flatMap(transformModifier).toList
  val methodModifiers = if (context.isInterface)
    originalMethodModifiers ::: List(ast.PublicKeyword(), ast.AbstractKeyword())
  else
    originalMethodModifiers

  ast.MethodDeclaration(
    transformName(decl.getName),
    `type` = transformType(context, decl.getType),
    typeParameters = decl.getTypeParameters.asScala.map(transformType.curried(context)).map {
      t => t.get
    }.toList,
    parameters = methodParameters,
    body = methodBody,
    modifiers = methodModifiers
  )

def transformParameter(context: FileContext, param: Parameter) =
  ast.Parameter(transformName(param.getName), transformType(context, param.getType))

def createStructuredCloneMethod(`type`: SimpleName) = ast.MethodDeclaration(
  ast.Identifier("clone"),
  Some(ast.TypeReference(ast.Identifier(`type`.asString))),
  List(),
  List(),
  Some(
    ast.Block(
      List(
        ast.ReturnStatement(
          Some(
            ast.CallExpression(ast.Identifier("structuredClone"), List(ast.ThisKeyword()))
          )
        )
      )
    )
  ),
  List(ast.PublicKeyword())
)
