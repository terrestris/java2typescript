package de.terrestris.java2typescript.transformer

import com.github.javaparser.ast.Modifier.Keyword
import com.github.javaparser.ast.{CompilationUnit, ImportDeclaration, Modifier, NodeList, PackageDeclaration}
import com.github.javaparser.ast.`type`.{ClassOrInterfaceType, Type, VoidType}
import com.github.javaparser.ast.body.{BodyDeclaration, ClassOrInterfaceDeclaration, FieldDeclaration, MethodDeclaration, Parameter, TypeDeclaration, VariableDeclarator}
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.expr.BinaryExpr.Operator
import com.github.javaparser.ast.stmt.{BlockStmt, ExpressionStmt, IfStmt, LocalClassDeclarationStmt, ReturnStmt, Statement}
import de.terrestris.java2typescript.ast
import de.terrestris.java2typescript.util.resolveImportPath

import java.util.Optional
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

def transformCompilationUnit(cu: CompilationUnit): List[ast.Node] =
  cu.getImports.asScala.map(i => transformImport(cu.getPackageDeclaration, i)).toList
  :::
  cu.getTypes.asScala.map(t => transformTypeDeclaration(t, List(ast.ExportKeyword()))).toList

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

def transformTypeDeclaration(decl: TypeDeclaration[?], modifiers: List[ast.Modifier] = List()) =
  decl match
    case decl: ClassOrInterfaceDeclaration => transformClassOrInterfaceDeclaration(decl, modifiers)
    case _ => throw new Error("not supported")

def transformBlockStatement(stmt: BlockStmt): ast.Block =
  ast.Block(stmt.getStatements.asScala.map(transformStatement).toList)

def transformStatement(stmt: Statement): ast.Statement =
  stmt match
    case stmt: ExpressionStmt =>
      val expr = transformExpression(stmt.getExpression)
      expr match
        case expr: ast.VariableDeclarationList => ast.VariableStatement(expr)
        case expr => ast.ExpressionStatement(expr)
    case stmt: LocalClassDeclarationStmt => transformClassOrInterfaceDeclaration(stmt.getClassDeclaration)
    case stmt: ReturnStmt => ast.ReturnStatement(stmt.getExpression.toScala.map(transformExpression))
    case stmt: IfStmt => transformIfStatement(stmt)
    case stmt: BlockStmt => transformBlockStatement(stmt)
    case _ => throw new Error("not supported")

def transformIfStatement(stmt: IfStmt) =
  ast.IfStatement(
    transformExpression(stmt.getCondition),
    transformStatement(stmt.getThenStmt),
    stmt.getElseStmt.toScala.map(transformStatement)
  )

def transformClassOrInterfaceDeclaration(decl: ClassOrInterfaceDeclaration, additionalModifiers: List[ast.Modifier] = List()) =
  val className = decl.getName.getIdentifier
  val memberVals = decl.getMembers.asScala.map(m => transformMember(m, className)).toList
  if (decl.isInterface)
    ast.InterfaceDeclaration(transformName(decl.getName), members = memberVals, modifiers = additionalModifiers)
  else
    ast.ClassDeclaration(transformName(decl.getName), members = memberVals, modifiers = additionalModifiers)

def transformMember(member: BodyDeclaration[?], className: String): ast.Member =
  member match
    case member: FieldDeclaration =>
      val variables = member.getVariables.asScala.toList
      if (variables.length != 1)
        throw new Error(s"amount of variables in member not supported (${variables.length})")
      transformDeclaratorToProperty(variables.head, member.getModifiers.asScala.toList)
    case member: MethodDeclaration =>
      transformMethodDeclaration(member, className)

def transformMethodDeclaration(decl: MethodDeclaration, className: String) =
  if (decl.getName.getIdentifier == className)
    ast.Constructor(
      parameters = decl.getParameters.asScala.map(transformParameter).toList,
      body = decl.getBody.toScala.map(body => ast.Block(body.getStatements.asScala.map(transformStatement).toList)),
      modifiers = decl.getModifiers.asScala.map(transformModifier).toList
    )
  else
    ast.MethodDeclaration(
      transformName(decl.getName),
      `type` = transformType(decl.getType),
      typeParameters = decl.getTypeParameters.asScala.map(transformType).toList,
      parameters = decl.getParameters.asScala.map(transformParameter).toList,
      body = decl.getBody.toScala.map(body => ast.Block(body.getStatements.asScala.map(transformStatement).toList)),
      modifiers = decl.getModifiers.asScala.map(transformModifier).toList
    )

def transformParameter(param: Parameter) =
  ast.Parameter(transformName(param.getName), transformType(param.getType))

def transformExpression(expr: Expression): ast.Expression =
  expr match
    case expr: VariableDeclarationExpr =>
      ast.VariableDeclarationList(
        expr.getVariables.asScala.map(transformDeclaratorToVariable).toList
      )
    case expr: LiteralExpr => transformLiteral(expr)
    case expr: ObjectCreationExpr => transformObjectCreationExpression(expr)
    case expr: BinaryExpr => transformBinaryExpression(expr)
    case expr: UnaryExpr => transformUnaryExpression(expr)
    case expr: EnclosedExpr => ast.ParenthesizedExpression(transformExpression(expr.getInner))
    case expr: NameExpr => transformName(expr.getName)
    case expr: AssignExpr => transformAssignExpression(expr)
    case expr: FieldAccessExpr => transformFieldAccessExpression(expr)
    case expr: ThisExpr => ast.ThisKeyword()
    case _ => throw new Error("not supported")

def transformFieldAccessExpression(expr: FieldAccessExpr) =
  ast.PropertyAccessExpression(
    transformExpression(expr.getScope),
    transformName(expr.getName)
  )

def transformAssignExpression(expr: AssignExpr) =
  ast.BinaryExpression(transformExpression(expr.getTarget), transformExpression(expr.getValue), ast.EqualsToken())

def transformOperator(op: BinaryExpr.Operator|UnaryExpr.Operator): ast.Token =
  op.name match
    case "PLUS" => ast.PlusToken()
    case "MINUS" => ast.MinusToken()
    case "MULTIPLY" => ast.AsteriskToken()
    case "DIVIDE" => ast.SlashToken()
    case "AND" => ast.AmpersandAmpersandToken()
    case "OR" => ast.BarBarToken()
    case "EQUALS" => ast.EqualsEqualsEqualsToken()
    case "NOT_EQUALS" => ast.ExclamationEqualsEqualsToken()
    case "LESS_EQUALS" => ast.LessThanEqualsToken()
    case "LESS" => ast.LessThanToken()
    case "GREATER" => ast.GreaterThanToken()
    case "GREATER_EQUALS" => ast.GreaterThanEqualsToken()
    case _ => throw new Error("not supported")

def transformBinaryExpression(expr: BinaryExpr): ast.BinaryExpression =
  ast.BinaryExpression(transformExpression(expr.getLeft), transformExpression(expr.getRight), transformOperator(expr.getOperator))

def transformUnaryExpression(expr: UnaryExpr): ast.PrefixUnaryExpression =
  ast.PrefixUnaryExpression(transformOperator(expr.getOperator).kind, transformExpression(expr.getExpression))

def transformObjectCreationExpression(expr: ObjectCreationExpr) =
  ast.NewExpression(
    ast.Identifier(expr.getType.getName.getIdentifier),
    transformArguments(expr.getArguments),
    transformTypeArguments(expr.getTypeArguments)
  )

def transformArguments(expressions: NodeList[Expression]) =
  expressions.asScala.map(transformExpression).toList

def transformDeclaratorToVariable(decl: VariableDeclarator): ast.VariableDeclaration =
  ast.VariableDeclaration(
    transformName(decl.getName),
    transformType(decl.getType),
    decl.getInitializer.toScala.map(transformExpression)
  )

def transformDeclaratorToProperty(decl: VariableDeclarator, modifiers: List[Modifier]): ast.PropertyDeclaration =
  ast.PropertyDeclaration(
    transformName(decl.getName),
    transformType(decl.getType),
    decl.getInitializer.toScala.map(transformExpression),
    modifiers.map(transformModifier)
  )

def transformModifier(modifier: Modifier): ast.Modifier =
  modifier.getKeyword match
    case Keyword.PUBLIC => ast.PublicKeyword()
    case Keyword.PROTECTED => ast.ProtectedKeyword()
    case Keyword.PRIVATE => ast.PrivateKeyword()
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
    case _ => throw new Error("not supported")
