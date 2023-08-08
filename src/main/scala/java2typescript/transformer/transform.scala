package de.terrestris.java2typescript
package transformer

import com.github.javaparser.ast.{CompilationUnit, ImportDeclaration, NodeList, PackageDeclaration}
import com.github.javaparser.ast.`type`.{ClassOrInterfaceType, Type}
import com.github.javaparser.ast.body.{ClassOrInterfaceDeclaration, TypeDeclaration, VariableDeclarator}
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.expr.BinaryExpr.Operator
import com.github.javaparser.ast.stmt.{BlockStmt, ExpressionStmt, LocalClassDeclarationStmt, Statement}
import de.terrestris.java2typescript.util.resolveImportPath

import java.util.Optional
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

def transformCompilationUnit(cu: CompilationUnit): List[ast.Node] = {
  cu.getImports.asScala.map(i => transformImport(cu.getPackageDeclaration, i)).toList
  :::
  cu.getTypes.asScala.map(t => transformTypeDeclaration(t, List(ast.ExportKeyword()))).toList
}

def transformImport(pack: Optional[PackageDeclaration], importDeclaration: ImportDeclaration) = {
  val packagePath = pack.map(p => p.getName.toString.split("\\.")).orElse(Array[String]())
  val importPath = importDeclaration.getName.getQualifier.map(q => q.toString.split("\\.")).orElse(Array[String]())
  val resolvedPath = resolveImportPath(packagePath, importPath)
  val identifier = importDeclaration.getName.getIdentifier
  ast.ImportDeclaration(
    ast.ImportClause(
      ast.NamedImports(
        List(
          ast.ImportSpecifier(
            ast.Identifier(identifier)
          )
        )
      )
    ),
    ast.StringLiteral(
      s"$resolvedPath/$identifier.ts"
    )
  )
}

def transformTypeDeclaration[T <: TypeDeclaration[?]](decl: TypeDeclaration[T], modifiers: List[ast.Modifier] = List()) =
  decl match
    case decl: ClassOrInterfaceDeclaration => transformClassOrInterfaceDeclaration(decl, modifiers)
    case _ => throw new Error("not supported")

def transformBlockStatement(stmt: BlockStmt): List[ast.Node] =
  stmt.getStatements.asScala.map(transformStatement).toList

def transformStatement(stmt: Statement): ast.Node =
  stmt match
    case stmt: ExpressionStmt =>
      val expr = transformExpression(stmt.getExpression)
      expr match
        case expr: ast.VariableDeclarationList => ast.VariableStatement(expr)
        case _ => throw new Error("not supported")
    case stmt: LocalClassDeclarationStmt => transformClassOrInterfaceDeclaration(stmt.getClassDeclaration)
    case _ => throw new Error("not supported")

def transformClassOrInterfaceDeclaration(decl: ClassOrInterfaceDeclaration, modifiersArg: List[ast.Modifier] = List()) =
  if (decl.isInterface)
    ast.InterfaceDeclaration(transformName(decl.getName), modifiers = modifiersArg)
  else
    ast.ClassDeclaration(transformName(decl.getName), modifiers = modifiersArg)

def transformExpression(expr: Expression): ast.Expression =
  expr match
    case expr: VariableDeclarationExpr =>
      ast.VariableDeclarationList(
        expr.getVariables.asScala.map(transformDeclarator).toList
      )
    case expr: LiteralExpr => transformLiteral(expr)
    case expr: ObjectCreationExpr => transformObjectCreationExpression(expr)
    case expr: BinaryExpr => transformBinaryExpression(expr)
    case expr: UnaryExpr => transformUnaryExpression(expr)
    case expr: EnclosedExpr => ast.ParenthesizedExpression(transformExpression(expr.getInner))
    case _ => throw new Error("not supported")

def transformOperator(op: BinaryExpr.Operator|UnaryExpr.Operator): ast.Token =
  op.name match
    case "PLUS" => ast.PlusToken()
    case "MINUS" => ast.MinusToken()
    case "MULTIPLY" => ast.AsteriskToken()
    case "DIVIDE" => ast.SlashToken()
    case _ => throw new Error("not supported")

def transformBinaryExpression(expr: BinaryExpr): ast.BinaryExpression =
  ast.BinaryExpression(transformExpression(expr.getLeft), transformExpression(expr.getRight), transformOperator(expr.getOperator))

def transformUnaryExpression(expr: UnaryExpr): ast.PrefixUnaryExpression =
  ast.PrefixUnaryExpression(transformOperator(expr.getOperator).kind, transformExpression(expr.getExpression))

def transformObjectCreationExpression(expr: ObjectCreationExpr) = {
  val ident = ast.Identifier(expr.getType.getName.getIdentifier)
  val types = transformTypeArguments(expr.getTypeArguments)
  val arguments = transformArguments(expr.getArguments)
  ast.NewExpression(ident, arguments, types)
}

def transformArguments(expressions: NodeList[Expression]) =
  expressions.asScala.map(transformExpression).toList

def transformDeclarator(decl: VariableDeclarator): ast.VariableDeclaration =
  ast.VariableDeclaration(
    transformName(decl.getName),
    transformType(decl.getType),
    decl.getInitializer.toScala.map(transformExpression)
  )

def transformName(name: SimpleName): ast.Identifier =
  ast.Identifier(name.getIdentifier)

def transformType(aType: Type): ast.Type =
  aType match
    case aType: ClassOrInterfaceType =>
      aType.getName.getIdentifier match
        case "String" => ast.StringKeyword()
        case "Integer"|"Double" => ast.NumberKeyword()
        case other => ast.TypeReference(ast.Identifier(other), transformTypeArguments(aType.getTypeArguments))
    case _ => throw new Error("not supported")

def transformTypeArguments(args: Optional[NodeList[Type]]): List[ast.Type] =
  args.map(o => o.asScala.map(transformType).toList).orElse(List[ast.Type]())

def transformLiteral(expr: LiteralExpr): ast.Literal =
  expr match
    case expr: StringLiteralExpr =>
      ast.StringLiteral(expr.getValue)
    case expr: (IntegerLiteralExpr|DoubleLiteralExpr) =>
      ast.NumericLiteral(expr.getValue)
    case _ => throw new Error("not supported")
