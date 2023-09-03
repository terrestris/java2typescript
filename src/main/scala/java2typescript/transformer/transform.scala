package de.terrestris.java2typescript.transformer

import com.github.javaparser.ast.Modifier.Keyword
import com.github.javaparser.ast.{CompilationUnit, ImportDeclaration, Modifier, NodeList, PackageDeclaration}
import com.github.javaparser.ast.`type`.{ArrayType, ClassOrInterfaceType, PrimitiveType, Type, VoidType}
import com.github.javaparser.ast.body.{BodyDeclaration, ClassOrInterfaceDeclaration, ConstructorDeclaration, FieldDeclaration, MethodDeclaration, Parameter, TypeDeclaration, VariableDeclarator}
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.stmt.{BlockStmt, BreakStmt, ContinueStmt, ExpressionStmt, ForStmt, IfStmt, ReturnStmt, Statement, ThrowStmt, WhileStmt}
import de.terrestris.java2typescript.{Config, ast}
import de.terrestris.java2typescript.ast.SyntaxKind

import java.util.Optional
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

class Context(
  val classOrInterface: ClassOrInterfaceDeclaration
) {
  val internalClasses: mutable.Buffer[ast.ClassDeclaration|ast.InterfaceDeclaration] = mutable.Buffer()
  def isMember(name: SimpleName): Boolean =
    classOrInterface.getMembers.asScala
      .exists(mem => mem match
        case mem: FieldDeclaration =>
          mem.getVariables.asScala.exists(v => v.getName == name)
        case mem: MethodDeclaration =>
          mem.getName == name
        case _ => false
      )
}

def transformCompilationUnit(config: Config, cu: CompilationUnit): List[ast.Node] =
  cu.getImports.asScala.map(i => transformImport(config, cu.getPackageDeclaration, i)).toList
  :::
  cu.getTypes.asScala.flatMap(t => transformTypeDeclaration(t, List(ast.ExportKeyword()))).toList

def transformTypeDeclaration(decl: TypeDeclaration[?], modifiers: List[ast.Modifier] = List()) =
  decl match
    case decl: ClassOrInterfaceDeclaration => transformClassOrInterfaceDeclaration(decl, modifiers)
    case _ => throw new Error("not supported")

def transformNameInContext(context: Context, name: SimpleName) =
  if (context.isMember(name))
    ast.PropertyAccessExpression(
      ast.ThisKeyword(),
      transformName(name)
    )
  else
    transformName(name)

def transformDeclaratorToVariable(context: Context, decl: VariableDeclarator): ast.VariableDeclaration =
  ast.VariableDeclaration(
    transformName(decl.getName),
    transformType(decl.getType),
    decl.getInitializer.toScala.map(transformExpression.curried(context))
  )

def transformDeclaratorToProperty(context: Context, decl: VariableDeclarator, modifiers: List[Modifier]): ast.PropertyDeclaration =
  ast.PropertyDeclaration(
    transformName(decl.getName),
    transformType(decl.getType),
    decl.getInitializer.toScala.map(transformExpression.curried(context)),
    modifiers.flatMap(transformModifier)
  )

def transformModifier(modifier: Modifier): Option[ast.Modifier] =
  modifier.getKeyword match
    case Keyword.PUBLIC => Some(ast.PublicKeyword())
    case Keyword.PROTECTED => Some(ast.ProtectedKeyword())
    case Keyword.PRIVATE => Some(ast.PrivateKeyword())
    case Keyword.STATIC => Some(ast.StaticKeyword())
    case Keyword.FINAL => None
    case key => throw new Error(s"Modifier $key not supported")

def transformName(name: SimpleName): ast.Identifier =
  ast.Identifier(name.getIdentifier)

def transformType(aType: Type): ast.Type =
  aType match
    case aType: ClassOrInterfaceType =>
      aType.getName.getIdentifier match
        case "Boolean" => ast.BooleanKeyword()
        case "String" => ast.StringKeyword()
        case "Integer"|"Double" => ast.NumberKeyword()
        case other => ast.TypeReference(ast.Identifier(other), transformTypeArguments(aType.getTypeArguments))
    case aType: VoidType => ast.VoidKeyword()
    case aType: ArrayType => ast.ArrayType(transformType(aType.getComponentType))
    case aType: PrimitiveType =>
      transformType(aType.toBoxedType)
    case _ => throw new Error("not supported")

def transformTypeArguments(args: Optional[NodeList[Type]]): List[ast.Type] =
  args.map(o => o.asScala.map(transformType).toList).orElse(List[ast.Type]())

def transformLiteral(expr: LiteralExpr): ast.Literal =
  expr match
    case expr: StringLiteralExpr =>
      ast.StringLiteral(expr.getValue)
    case expr: (IntegerLiteralExpr|DoubleLiteralExpr) =>
      ast.NumericLiteral(expr.getValue)
    case expr: BooleanLiteralExpr =>
      if (expr.getValue)
        ast.TrueKeyword()
      else
        ast.FalseKeyword()
    case expr: NullLiteralExpr => ast.NullKeyword()
    case _ => throw new Error("not supported")
