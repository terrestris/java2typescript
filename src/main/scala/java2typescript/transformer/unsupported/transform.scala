package java2typescript.transformer.unsupported

import java2typescript.ast
import java2typescript.ast.SyntaxKind
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.{BodyDeclaration, ClassOrInterfaceDeclaration, ConstructorDeclaration, MethodDeclaration, TypeDeclaration}
import java2typescript.transformer.{Context, createConstructorOverloads, createMethodOverloads, groupMethodsByName, transformHeritage, transformModifier, transformName, transformParameter, transformType}

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

val unsupportedKeywords = List(
  "synchronized",
  "volatile",
  "transient"
)

def checkUnsupported(code: String) =
  val reduced = code
    .replaceAll("/\\*[\\S\\s]*?\\*/", "") // remove multiline comments
    .replaceAll("\\\\\"", "") // remove escaped quotes
    .replaceAll("\"[^\"]*\"", "\"\"") // remove everything between quotes
    .replaceAll("//.*", "") // remove single line comments
    .toLowerCase()

  unsupportedKeywords.exists {
    keyword => reduced.contains(keyword)
  }

def transformCompilationUnit(cu: CompilationUnit): List[ast.Node] =
  cu.getTypes.asScala.map(t => transformTypeDeclaration(t, List(ast.ExportKeyword()))).toList

def transformTypeDeclaration(decl: TypeDeclaration[?], modifiers: List[ast.Modifier] = List()) =
  decl match
    case decl: ClassOrInterfaceDeclaration => transformClassOrInterfaceDeclaration(decl, modifiers)
    case _ => throw new Error("not supported")

val unsupportedBody = ast.Block(
  List(
    ast.ThrowStatement(
      ast.NewExpression(
        ast.Identifier("Error"),
        List(
          ast.StringLiteral("This class uses features that are not supported by javascript")
        )
      )
    )
  )
)

def unsupportedConstructor(declaration: ConstructorDeclaration) =
  val methodParameters = declaration.getParameters.asScala.map(transformParameter).toList
  val methodModifiers = declaration.getModifiers.asScala.flatMap(transformModifier).toList
  ast.Constructor(
    methodParameters,
    Option(unsupportedBody),
    methodModifiers
  )

def unsupportedStaticMethod(decl: MethodDeclaration) =
  val methodParameters = decl.getParameters.asScala.map(transformParameter).toList
  val methodModifiers = decl.getModifiers.asScala.flatMap(transformModifier).toList

  ast.MethodDeclaration(
    transformName(decl.getName),
    `type` = transformType(decl.getType),
    typeParameters = decl.getTypeParameters.asScala.map(transformType).toList,
    parameters = methodParameters,
    body = Option(unsupportedBody),
    modifiers = methodModifiers
  )


def transformMember(member: BodyDeclaration[?]): List[ast.Member] =
  member match
    case member: MethodDeclaration =>
      if (!member.isStatic)
        return List()
      List(unsupportedStaticMethod(member))
    case member: ConstructorDeclaration =>
      List(unsupportedConstructor(member))
    case default => List()

def transformClassOrInterfaceDeclaration(
  decl: ClassOrInterfaceDeclaration,
  additionalModifiers: List[ast.Modifier] = List()
): ast.Statement =
  if (decl.isInterface)
    throw new Error("Interface should always be supported")

  val className = decl.getName.getIdentifier

  val members = decl.getMembers.asScala.flatMap(transformMember)

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
    members = constructorsWithOverloads ::: methodsWithOverloads,
    modifiers = modifiersVal,
    heritageClauses = transformHeritage(decl.getExtendedTypes, SyntaxKind.ExtendsKeyword).toList
      ::: transformHeritage(decl.getImplementedTypes, SyntaxKind.ImplementsKeyword).toList
  )
