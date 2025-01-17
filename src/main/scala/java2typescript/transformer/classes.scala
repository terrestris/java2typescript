package java2typescript.transformer

import com.github.javaparser.ast.Modifier.Keyword
import com.github.javaparser.ast.{Modifier, NodeList}
import com.github.javaparser.ast.`type`.ClassOrInterfaceType
import com.github.javaparser.ast.body.{BodyDeclaration, ClassOrInterfaceDeclaration, ConstructorDeclaration, EnumDeclaration, FieldDeclaration, InitializerDeclaration, MethodDeclaration, Parameter}
import java2typescript.ast
import java2typescript.ast.SyntaxKind

import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

def transformHeritage(context: FileContext, ancestors: List[ClassOrInterfaceType], token: ast.SyntaxKind): Option[ast.HeritageClause] =
  if (ancestors.nonEmpty)
    Some(
      ast.HeritageClause(
        ancestors.map {
          t =>
            context.addImportIfNeeded(None, t.getName.asString)
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
  context: FileContext,
  decl: ClassOrInterfaceDeclaration,
  additionalModifiers: List[ast.Modifier] = List(),
  dropModifiers: Boolean = false
): List[ast.Statement] =
  val className = decl.getName.getIdentifier
  val classContext = context match
    case c: ClassContext => ClassContext(c, Some(decl), parentClassContext = Option(c))
    case c: FileContext => ClassContext(c, Some(decl))

  val members = decl.getMembers.asScala
    .flatMap(transformMember.curried(classContext))

  val extractedStatements = classContext
    .extractedStatements
    .toList

  val modifiersVal = getModifiers(decl.getModifiers.asScala.toList, additionalModifiers)

  val typeParameters = decl.getTypeParameters.asScala.map(transformType.curried(classContext)).map {
    t => t.get
  }.toList

  if (decl.isInterface)
    ast.ClassDeclaration(
      transformName(decl.getName),
      typeParameters = typeParameters,
      members = members.toList,
      modifiers = modifiersVal ::: List(ast.AbstractKeyword()),
      heritageClauses = transformHeritage(classContext, decl.getExtendedTypes.asScala.toList, ast.SyntaxKind.ExtendsKeyword).toList
        ::: transformHeritage(classContext, decl.getImplementedTypes.asScala.filter(t => !isDroppableInterface(t.getName)).toList, ast.SyntaxKind.ImplementsKeyword).toList
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

    val methodsWithOverloads = groupMethods(members
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
      typeParameters = typeParameters,
      members = properties ::: constructorsWithOverloads ::: methodsWithOverloads,
      modifiers = modifiersVal,
      heritageClauses = transformHeritage(classContext, decl.getExtendedTypes.asScala.toList, ast.SyntaxKind.ExtendsKeyword).toList
        ::: transformHeritage(classContext, decl.getImplementedTypes.asScala.filter(t => !isDroppableInterface(t.getName)).toList, ast.SyntaxKind.ImplementsKeyword).toList
    )
      ::
      extractedStatements
  }

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
