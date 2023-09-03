package de.terrestris.java2typescript.transformer

import com.github.javaparser.ast.body.{BodyDeclaration, ClassOrInterfaceDeclaration, ConstructorDeclaration, FieldDeclaration, MethodDeclaration, Parameter}
import de.terrestris.java2typescript.ast

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

def transformClassOrInterfaceDeclaration(
  decl: ClassOrInterfaceDeclaration,
  additionalModifiers: List[ast.Modifier] = List()
): List[ast.ClassDeclaration|ast.InterfaceDeclaration] =
  val className = decl.getName.getIdentifier
  val context = Context(decl)
  val memberVals = decl.getMembers.asScala.flatMap(transformMember.curried(context))

  if (decl.isInterface)
    ast.InterfaceDeclaration(transformName(decl.getName), members = memberVals.toList, modifiers = additionalModifiers)
      ::
      context.internalClasses.toList
  else {
    val properties = memberVals
      .collect {
        case p: ast.PropertyDeclaration => p
      }
      .toList

    val constructors = memberVals
      .collect {
        case c: ast.Constructor => c
      }
      .toList

    val constructorsWithOverloads =
      if (constructors.length > 1)
        createConstructorOverloads(constructors)
      else
        constructors

    val methodsWithOverloads = groupMethodsByName(memberVals
      .collect {
        case m: ast.MethodDeclaration => m
      }.toList)
      .flatMap {
        ms =>
          if (ms.length > 1)
            createMethodOverloads(ms)
          else
            ms
      }

    ast.ClassDeclaration(
      transformName(decl.getName),
      members = properties ::: constructorsWithOverloads ::: methodsWithOverloads,
      modifiers = additionalModifiers
    )
      ::
      context.internalClasses.toList
  }

def transformMember(context: Context, member: BodyDeclaration[?]): Option[ast.Member] =
  member match
    case member: FieldDeclaration =>
      val variables = member.getVariables.asScala.toList
      if (variables.length != 1)
        throw new Error(s"amount of variables in member not supported (${variables.length})")
      Some(transformDeclaratorToProperty(context, variables.head, member.getModifiers.asScala.toList))
    case member: MethodDeclaration =>
      Some(transformMethodDeclaration(context, member))
    case member: ConstructorDeclaration
    => Some(transformConstructorDeclaration(context, member))
    case member: ClassOrInterfaceDeclaration =>
      context.internalClasses.appendAll(transformClassOrInterfaceDeclaration(member))
      None

def transformConstructorDeclaration(context: Context, declaration: ConstructorDeclaration) =
  val methodBody = ast.Block(
    declaration.getBody.getStatements.asScala.map(transformStatement.curried(context)).toList
  )
  val methodParameters = declaration.getParameters.asScala.map(transformParameter).toList
  val methodModifiers = declaration.getModifiers.asScala.flatMap(transformModifier).toList

  ast.Constructor(
    parameters = methodParameters,
    body = Some(methodBody),
    modifiers = methodModifiers
  )

def transformMethodDeclaration(context: Context, decl: MethodDeclaration) =
  val methodBody = decl.getBody.toScala.map(body =>
    ast.Block(body.getStatements.asScala.map(transformStatement.curried(context)).toList)
  )
  val methodParameters = decl.getParameters.asScala.map(transformParameter).toList
  val methodModifiers = decl.getModifiers.asScala.flatMap(transformModifier).toList

  ast.MethodDeclaration(
    transformName(decl.getName),
    `type` = transformType(decl.getType),
    typeParameters = decl.getTypeParameters.asScala.map(transformType).toList,
    parameters = methodParameters,
    body = methodBody,
    modifiers = methodModifiers
  )

def transformParameter(param: Parameter) =
  ast.Parameter(transformName(param.getName), transformType(param.getType))
