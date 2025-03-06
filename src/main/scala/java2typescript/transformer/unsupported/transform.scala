package java2typescript.transformer.unsupported

import java2typescript.ast
import java2typescript.ast.SyntaxKind
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.{BodyDeclaration, ClassOrInterfaceDeclaration, ConstructorDeclaration, MethodDeclaration, TypeDeclaration}
import java2typescript.transformer.{FileContext, ProjectContext, createConstructorOverloads, createMethodOverloads, groupMethodsByName, isDroppableInterface, transformHeritage, transformModifier, transformName, transformParameter, transformType}

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

val unsupportedWords = List(
  "SoftReference",
  "synchronized"
)

def checkUnsupported(code: String) =
  val reduced = code
    .replaceAll("/\\*[\\S\\s]*?\\*/", "") // remove multiline comments
    .replaceAll("\\\\\"", "") // remove escaped quotes
    .replaceAll("\"[^\"]*\"", "\"\"") // remove everything between quotes
    .replaceAll("//.*", "") // remove single line comments

  unsupportedWords.exists {
    word => reduced.contains(word)
  }

def transformCompilationUnit(context: ProjectContext, cu: CompilationUnit): List[ast.Node] =
  val packageName = cu.getPackageDeclaration.toScala.map { pd => pd.getName.asString() }
  val fileContext = FileContext(context, packageName)
  cu.getTypes.asScala.map(t => transformTypeDeclaration(fileContext, t, List(ast.ExportKeyword()))).toList

def transformTypeDeclaration(context: FileContext, decl: TypeDeclaration[?], modifiers: List[ast.Modifier] = List()) =
  decl match
    case decl: ClassOrInterfaceDeclaration => transformClassOrInterfaceDeclaration(context, decl, modifiers)
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

def unsupportedConstructor(context: FileContext, declaration: ConstructorDeclaration) =
  val methodParameters = declaration.getParameters.asScala.map(transformParameter.curried(context)).toList
  val methodModifiers = declaration.getModifiers.asScala.flatMap(transformModifier).toList
  ast.Constructor(
    methodParameters,
    Option(unsupportedBody),
    methodModifiers
  )

def unsupportedStaticMethod(context: FileContext, decl: MethodDeclaration) =
  val methodParameters = decl.getParameters.asScala.map(transformParameter.curried(context)).toList
  val methodModifiers = decl.getModifiers.asScala.flatMap(transformModifier).toList

  ast.MethodDeclaration(
    transformName(decl.getName),
    `type` = transformType(context, decl.getType),
    typeParameters = decl.getTypeParameters.asScala.map(transformType.curried(context)).map {
      t => t.get
    }.toList,
    parameters = methodParameters,
    body = Option(unsupportedBody),
    modifiers = methodModifiers
  )


def transformMember(context: FileContext, member: BodyDeclaration[?]): List[ast.Member] =
  member match
    case member: MethodDeclaration =>
      if (!member.isStatic)
        return List()
      List(unsupportedStaticMethod(context, member))
    case member: ConstructorDeclaration =>
      List(unsupportedConstructor(context, member))
    case default => List()

def transformClassOrInterfaceDeclaration(
  context: FileContext,
  decl: ClassOrInterfaceDeclaration,
  additionalModifiers: List[ast.Modifier] = List()
): ast.Statement =
  if (decl.isInterface)
    throw new Error("Interface should always be supported")

  val className = decl.getName.getIdentifier

  val members = decl.getMembers.asScala.flatMap(transformMember.curried(context))

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
    heritageClauses = transformHeritage(context, decl.getExtendedTypes.asScala.toList, SyntaxKind.ExtendsKeyword).toList
      ::: transformHeritage(context, decl.getImplementedTypes.asScala.filter(t => !isDroppableInterface(t.getName)).toList, SyntaxKind.ImplementsKeyword).toList
  )
