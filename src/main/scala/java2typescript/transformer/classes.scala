package de.terrestris
package java2typescript.transformer

import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.`type`.ClassOrInterfaceType
import com.github.javaparser.ast.body.{BodyDeclaration, ClassOrInterfaceDeclaration, ConstructorDeclaration, FieldDeclaration, InitializerDeclaration, MethodDeclaration, Parameter}
import de.terrestris.java2typescript.ast
import de.terrestris.java2typescript.ast.SyntaxKind

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

def transformHeritage(nodes: NodeList[ClassOrInterfaceType], token: SyntaxKind): Option[ast.HeritageClause] =
  val implementedTypes = nodes.asScala.toList
  if (implementedTypes.nonEmpty)
    Some(
      ast.HeritageClause(
        implementedTypes.map {
          t =>
            ast.ExpressionWithTypeArguments(
              transformName(t.getName),
              t.getTypeArguments.toScala.toList
                .flatMap(nl => nl.asScala.toList)
                .map(transformType)
            )
        },
        token
      )
    )
  else
    None

def transformClassOrInterfaceDeclaration(
  decl: ClassOrInterfaceDeclaration,
  additionalModifiers: List[ast.Modifier] = List(),
  dropModifiers: Boolean = false
): List[ast.Statement] =
  val className = decl.getName.getIdentifier
  val context = Context(decl)
  val contextsAndMembers = decl.getMembers.asScala
    .map(transformMember.curried(context))

  val membersVal = contextsAndMembers.collect {
    case Right(ms) => ms
  }.flatten

  val extractedStatements = contextsAndMembers
    .collect {
      case Left(c) => c.extractedStatements
    }
    .flatMap {
      ics => ics
    }
    .toList

  val modifiersVal = (additionalModifiers ::: decl.getModifiers.asScala.toList.flatMap(transformModifier)).filter {
    m =>
      !List(
        SyntaxKind.PublicKeyword,
        SyntaxKind.ProtectedKeyword,
        SyntaxKind.PrivateKeyword,
        SyntaxKind.DefaultKeyword,
        SyntaxKind.StaticKeyword
      ).contains(m.kind)
  }

  if (decl.isInterface)
    ast.InterfaceDeclaration(
      transformName(decl.getName),
      members = membersVal.toList,
      modifiers = modifiersVal,
      heritageClauses = transformHeritage(decl.getExtendedTypes, SyntaxKind.ExtendsKeyword).toList
        ::: transformHeritage(decl.getImplementedTypes, SyntaxKind.ImplementsKeyword).toList
    )
      ::
      extractedStatements
  else {
    val properties = membersVal
      .collect {
        case p: ast.PropertyDeclaration => p
      }
      .toList

    val constructors = membersVal
      .collect {
        case c: ast.Constructor => c
      }
      .toList

    val constructorsWithOverloads =
      if (constructors.length > 1)
        createConstructorOverloads(constructors)
      else
        constructors

    val methodsWithOverloads = groupMethodsByName(membersVal
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
      modifiers = modifiersVal,
      heritageClauses = transformHeritage(decl.getExtendedTypes, SyntaxKind.ExtendsKeyword).toList
        ::: transformHeritage(decl.getImplementedTypes, SyntaxKind.ImplementsKeyword).toList
    )
      ::
      extractedStatements
  }

def transformMember(context: Context, member: BodyDeclaration[?]): Either[Context, List[ast.Member]] =
  member match
    case member: FieldDeclaration =>
      Right(
        member.getVariables.asScala.toList.map(declarator =>
          transformDeclaratorToProperty(context, declarator, member.getModifiers.asScala.toList)
        )
      )
    case member: MethodDeclaration =>
      Right(List(transformMethodDeclaration(context, member)))
    case member: ConstructorDeclaration =>
      Right(List(transformConstructorDeclaration(context, member)))
    case member: ClassOrInterfaceDeclaration =>
      Left(context.addExtractedStatements(transformClassOrInterfaceDeclaration(member)))
    case member: InitializerDeclaration =>
      if (!member.isStatic) {
        throw new Error("non static initializers as class members are not supported")
      }
      Left(context.addExtractedStatements(transformBlockStatement(context, member.getBody).statements))

def transformConstructorDeclaration(context: Context, declaration: ConstructorDeclaration) =
  val methodParameters = declaration.getParameters.asScala.map(transformParameter).toList
  val methodContext = context.addParameters(methodParameters)
  val methodBody = ast.Block(
    declaration.getBody.getStatements.asScala.map(transformStatement.curried(methodContext)).toList
  )
  val methodModifiers = declaration.getModifiers.asScala.flatMap(transformModifier).toList

  ast.Constructor(
    parameters = methodParameters,
    body = Some(methodBody),
    modifiers = methodModifiers
  )

def transformMethodDeclaration(context: Context, decl: MethodDeclaration) =
  val methodParameters = decl.getParameters.asScala.map(transformParameter).toList
  val methodContext = context.addParameters(methodParameters)
  val methodBody = decl.getBody.toScala.map(body =>
    ast.Block(body.getStatements.asScala.map(transformStatement.curried(methodContext)).toList)
  )
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
