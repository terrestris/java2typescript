package java2typescript.transformer

import com.github.javaparser.ast.Modifier.Keyword
import com.github.javaparser.ast.`type`.*
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.{CompilationUnit, Modifier, NodeList}
import java2typescript.{Config, ast}

import java.util.Optional
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

def transformCompilationUnit(context: ProjectContext, cu: CompilationUnit): List[ast.Node] =
  val packageName = cu.getPackageDeclaration.toScala.map { pd => pd.getName.asString() }

  val fileContext = FileContext(context, packageName)

  val types = cu.getTypes.asScala.flatMap(t => transformTypeDeclaration(fileContext, t, List(ast.ExportKeyword()))).toList

  createImports(fileContext) ::: types

def createImports(context: FileContext): List[ast.ImportDeclaration] =
  context.neededImports.map {
    im => transformImport(context.packageName, im)
  }.toList

def transformTypeDeclaration(context: FileContext, decl: TypeDeclaration[?], modifiers: List[ast.Modifier] = List()) =
  decl match
    case decl: ClassOrInterfaceDeclaration => transformClassOrInterfaceDeclaration(context, decl, modifiers)
    case decl: EnumDeclaration => List(transformEnumDeclaration(context, decl, modifiers))
    case _ => throw new Error("not supported")

def transformNameInContext(context: ParameterContext, name: SimpleName) =
  if (context.isNonStaticMember(name))
    ast.PropertyAccessExpression(
      ast.ThisKeyword(),
      transformName(name)
    )
  else if (context.isStaticMember(name))
    ast.PropertyAccessExpression(
      ast.Identifier(context.classOrInterface.getName.getIdentifier),
      transformName(name)
    )
  else
    transformName(name)

def transformDeclaratorToVariable(context: ClassContext|ParameterContext, decl: VariableDeclarator): ast.VariableDeclaration =
  val parameterContext = context match {
    case c: ParameterContext => c
    case c: ClassContext => ParameterContext(context, ListBuffer())
  }
  ast.VariableDeclaration(
    transformName(decl.getName),
    transformType(context, decl.getType),
    decl.getInitializer.toScala.map(transformExpression.curried(parameterContext))
  )

def transformDeclaratorToProperty(context: ClassContext, decl: VariableDeclarator, modifiers: List[Modifier]): ast.PropertyDeclaration =
  val parameterContext = ParameterContext(context, ListBuffer())
  ast.PropertyDeclaration(
    transformName(decl.getName),
    transformType(context, decl.getType),
    decl.getInitializer.toScala.map(transformExpression.curried(parameterContext)),
    modifiers.flatMap(transformModifier)
  )

def transformModifier(modifier: Modifier): Option[ast.Modifier] =
  modifier.getKeyword match
    case Keyword.PUBLIC => Some(ast.PublicKeyword())
    case Keyword.DEFAULT => Some(ast.PublicKeyword())
    case Keyword.PROTECTED => Some(ast.ProtectedKeyword())
    case Keyword.PRIVATE => Some(ast.PrivateKeyword())
    case Keyword.STATIC => Some(ast.StaticKeyword())
    case Keyword.ABSTRACT => Some(ast.AbstractKeyword())
    case Keyword.FINAL => None
    case key => throw new Error(s"Modifier $key not supported")

def transformName(name: SimpleName): ast.Identifier =
  ast.Identifier(name.getIdentifier)

def transformType(context: FileContext, aType: Type): Option[ast.Type] =
  aType match
    case aType: ClassOrInterfaceType =>
      aType.getName.getIdentifier match
        case "Boolean" => Some(ast.BooleanKeyword())
        case "String" => Some(ast.StringKeyword())
        case "Integer"|"Double" => Some(ast.NumberKeyword())
        case other =>
          context.addImportIfNeeded(aType.getName)
          Some(ast.TypeReference(ast.Identifier(other), transformTypeArguments(context, aType.getTypeArguments)))
    case aType: VoidType => Some(ast.VoidKeyword())
    case aType: ArrayType => Some(ast.ArrayType(transformType(context, aType.getComponentType).get))
    case aType: PrimitiveType => Some(transformType(context, aType.toBoxedType).get)
    case aType: WildcardType => None
    case aType: VarType => None
    case _ => throw new Error("not supported")

def transformTypeArguments(context: FileContext, args: Optional[NodeList[Type]]): List[ast.Type] =
  args.toScala.map(o => o.asScala.map(transformType.curried(context)).map {
    t => t.get
  }).toList.flatten

def transformLiteral(expr: LiteralExpr): ast.Literal =
  expr match
    case expr: StringLiteralExpr =>
      ast.StringLiteral(expr.getValue)
    case expr: (IntegerLiteralExpr|DoubleLiteralExpr|LongLiteralExpr) =>
      ast.NumericLiteral(expr.getValue)
    case expr: BooleanLiteralExpr =>
      if (expr.getValue)
        ast.TrueKeyword()
      else
        ast.FalseKeyword()
    case expr: NullLiteralExpr => ast.NullKeyword()
    case expr: CharLiteralExpr => ast.StringLiteral(expr.toString)
    case _ => throw new Error("not supported")
