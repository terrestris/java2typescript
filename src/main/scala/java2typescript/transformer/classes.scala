package java2typescript.transformer

import com.github.javaparser.ast.Modifier.Keyword
import com.github.javaparser.ast.{Modifier, NodeList}
import com.github.javaparser.ast.`type`.ClassOrInterfaceType
import com.github.javaparser.ast.body.{BodyDeclaration, ClassOrInterfaceDeclaration, ConstructorDeclaration, EnumDeclaration, FieldDeclaration, InitializerDeclaration, MethodDeclaration, Parameter}
import java2typescript.ast

import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

def transformHeritage(context: FileContext, nodes: NodeList[ClassOrInterfaceType], token: ast.SyntaxKind): Option[ast.HeritageClause] =
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
                .map(transformType.curried(context))
                .map {
                  t => t.get
                }
            )
        },
        token
      )
    )
  else
    None

def getModifiers(modifiers: List[Modifier], additionalModifiers: List[ast.Modifier]) = {
  (additionalModifiers ::: modifiers.flatMap(transformModifier)).filter {
    m =>
      !List(
        ast.SyntaxKind.PublicKeyword,
        ast.SyntaxKind.ProtectedKeyword,
        ast.SyntaxKind.PrivateKeyword,
        ast.SyntaxKind.DefaultKeyword,
        ast.SyntaxKind.StaticKeyword
      ).contains(m.kind)
  }
}

def transformClassOrInterfaceDeclaration(
  context: FileContext|ClassContext,
  decl: ClassOrInterfaceDeclaration,
  additionalModifiers: List[ast.Modifier] = List(),
  dropModifiers: Boolean = false
): List[ast.Statement] =
  val className = decl.getName.getIdentifier
  val classContext = context match
    case c: FileContext => ClassContext(c, Some(decl))
    case c: ClassContext => ClassContext(c, Some(decl), Option(c))
  val members = decl.getMembers.asScala
    .flatMap(transformMember.curried(classContext))

  val extractedStatements = classContext
    .extractedStatements
    .toList

  val modifiersVal = getModifiers(decl.getModifiers.asScala.toList, additionalModifiers)

  if (decl.isInterface)
    ast.InterfaceDeclaration(
      transformName(decl.getName),
      members = members.toList,
      modifiers = modifiersVal,
      heritageClauses = transformHeritage(context, decl.getExtendedTypes, ast.SyntaxKind.ExtendsKeyword).toList
        ::: transformHeritage(context, decl.getImplementedTypes, ast.SyntaxKind.ImplementsKeyword).toList
    )
      ::
      extractedStatements
  else {
    val properties = members
      .collect {
        case p: ast.PropertyDeclaration => p
      }
      .toList

    val constructors = members
      .collect {
        case c: ast.Constructor => c
      }
      .toList

    val constructorsWithOverloads =
      if (constructors.length > 1)
        createConstructorOverloads(constructors)
      else
        constructors

    val methodsWithOverloads = groupMethodsByName(members
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
      heritageClauses = transformHeritage(context, decl.getExtendedTypes, ast.SyntaxKind.ExtendsKeyword).toList
        ::: transformHeritage(context, decl.getImplementedTypes, ast.SyntaxKind.ImplementsKeyword).toList
    )
      ::
      extractedStatements
  }

def transformEnumDeclaration(
  context: FileContext|ClassContext,
  decl: EnumDeclaration,
  additionalModifiers: List[ast.Modifier] = List()
): List[ast.Statement] =
  val enumMembers = decl.getEntries.asScala.map(e => ast.EnumMember(transformName(e.getName))).toList
  val classContext = context match
    case c: FileContext => ClassContext(c, Some(decl))
    case c: ClassContext => ClassContext(c, Some(decl), Option(c))

  val otherMembers = decl.getMembers.asScala
    .flatMap(transformOtherEnumMember.curried(classContext))

  List(ast.EnumDeclaration(
    transformName(decl.getName),
    members = enumMembers,
    additionalModifiers
  )) ::: otherMembers.toList

def transformOtherEnumMember(context: ClassContext, member: BodyDeclaration[?]): List[ast.Statement] =
  member match
    case member: FieldDeclaration =>
      member.getVariables.asScala.toList.map(declarator =>
        ast.VariableStatement(ast.VariableDeclarationList(List(transformDeclaratorToVariable(context, declarator))))
      )
    case member: MethodDeclaration =>
      List(transformEnumMethodDeclaration(context, member))

def transformEnumMethodDeclaration(context: ClassContext, decl: MethodDeclaration): ast.Statement =
  val methodParameters = decl.getParameters.asScala.map(transformParameter.curried(context)).toList
  val methodContext = ParameterContext(context, methodParameters.toBuffer)
  val methodBody = decl.getBody.toScala.map(body =>
    ast.Block(body.getStatements.asScala.map(transformStatement.curried(methodContext)).toList)
  )
  val methodModifiers = if (decl.getModifiers.asScala.exists(m => m.getKeyword == Keyword.PUBLIC))
    List(ast.ExportKeyword())
  else
    List()

  ast.FunctionDeclaration(
    transformName(decl.getName),
    `type` = transformType(context, decl.getType),
    typeParameters = decl.getTypeParameters.asScala.map(transformType.curried(context)).map {
      t => t.get
    }.toList,
    parameters = methodParameters,
    body = methodBody,
    modifiers = methodModifiers
  )


def transformMember(context: ClassContext, member: BodyDeclaration[?]): List[ast.Member] =
  member match
    case member: FieldDeclaration =>
      member.getVariables.asScala.toList.map(declarator =>
        transformDeclaratorToProperty(context, declarator, member.getModifiers.asScala.toList)
      )
    case member: MethodDeclaration =>
      List(transformMethodDeclaration(context, member))
    case member: ConstructorDeclaration =>
      List(transformConstructorDeclaration(context, member))
    case member: ClassOrInterfaceDeclaration =>
      context.addExtractedStatements(transformClassOrInterfaceDeclaration(context, member, List(ast.ExportKeyword())))
      List()
    case member: InitializerDeclaration =>
      if (!member.isStatic) {
        throw new Error("non static initializers as class members are not supported")
      }
      val parameterContext = ParameterContext(context, ListBuffer())
      context.addExtractedStatements(transformBlockStatement(parameterContext, member.getBody).statements)
      List()

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

def transformMethodDeclaration(context: ClassContext, decl: MethodDeclaration) =
  val methodParameters = decl.getParameters.asScala.map(transformParameter.curried(context)).toList
  val methodContext = ParameterContext(context, methodParameters.toBuffer)
  val methodBody = decl.getBody.toScala.map(body =>
    ast.Block(body.getStatements.asScala.map(transformStatement.curried(methodContext)).toList)
  )
  val methodModifiers = decl.getModifiers.asScala.flatMap(transformModifier).toList

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
