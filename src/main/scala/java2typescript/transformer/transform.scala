package de.terrestris.java2typescript.transformer

import com.github.javaparser.ast.Modifier.Keyword
import com.github.javaparser.ast.{CompilationUnit, ImportDeclaration, Modifier, NodeList, PackageDeclaration}
import com.github.javaparser.ast.`type`.{ArrayType, ClassOrInterfaceType, PrimitiveType, Type, VoidType}
import com.github.javaparser.ast.body.{BodyDeclaration, ClassOrInterfaceDeclaration, FieldDeclaration, MethodDeclaration, Parameter, TypeDeclaration, VariableDeclarator}
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.expr.BinaryExpr.Operator
import com.github.javaparser.ast.stmt.{BlockStmt, ExpressionStmt, IfStmt, ReturnStmt, Statement}
import de.terrestris.java2typescript.ast
import de.terrestris.java2typescript.util.resolveImportPath

import java.util.Optional
import scala.::
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
      )
  def isConstructor(name: SimpleName): Boolean =
    classOrInterface.getName == name
}

def transformCompilationUnit(cu: CompilationUnit): List[ast.Node] =
  cu.getImports.asScala.map(i => transformImport(cu.getPackageDeclaration, i)).toList
  :::
  cu.getTypes.asScala.flatMap(t => transformTypeDeclaration(t, List(ast.ExportKeyword()))).toList

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

def transformBlockStatement(context: Context, stmt: BlockStmt): ast.Block =
  ast.Block(stmt.getStatements.asScala.map(transformStatement.curried(context)).toList)

def transformStatement(context: Context, stmt: Statement): ast.Statement =
  stmt match
    case stmt: ExpressionStmt =>
      val expr = transformExpression(context, stmt.getExpression)
      expr match
        case expr: ast.VariableDeclarationList => ast.VariableStatement(expr)
        case expr => ast.ExpressionStatement(expr)
//    case stmt: LocalClassDeclarationStmt => transformClassOrInterfaceDeclaration(stmt.getClassDeclaration)
    case stmt: ReturnStmt => ast.ReturnStatement(stmt.getExpression.toScala.map(transformExpression.curried(context)))
    case stmt: IfStmt => transformIfStatement(context, stmt)
    case stmt: BlockStmt => transformBlockStatement(context, stmt)
    case _ => throw new Error("not supported")

def transformIfStatement(context: Context, stmt: IfStmt) =
  ast.IfStatement(
    transformExpression(context, stmt.getCondition),
    transformStatement(context, stmt.getThenStmt),
    stmt.getElseStmt.toScala.map(transformStatement.curried(context))
  )

def transformClassOrInterfaceDeclaration(
  decl: ClassOrInterfaceDeclaration,
  additionalModifiers: List[ast.Modifier] = List()
): List[ast.ClassDeclaration|ast.InterfaceDeclaration] =
  val className = decl.getName.getIdentifier
  val context = Context(decl)
  val memberVals = decl.getMembers.asScala.flatMap(transformMember.curried(context)).toList
  if (decl.isInterface)
    ast.InterfaceDeclaration(transformName(decl.getName), members = memberVals, modifiers = additionalModifiers)
    ::
    context.internalClasses.toList
  else
    ast.ClassDeclaration(transformName(decl.getName), members = memberVals, modifiers = additionalModifiers)
    ::
    context.internalClasses.toList

def transformMember(context: Context, member: BodyDeclaration[?]): Option[ast.Member] =
  member match
    case member: FieldDeclaration =>
      val variables = member.getVariables.asScala.toList
      if (variables.length != 1)
        throw new Error(s"amount of variables in member not supported (${variables.length})")
      Some(transformDeclaratorToProperty(context, variables.head, member.getModifiers.asScala.toList))
    case member: MethodDeclaration =>
      Some(transformMethodDeclaration(context, member))
    case member: ClassOrInterfaceDeclaration =>
      context.internalClasses.appendAll(transformClassOrInterfaceDeclaration(member))
      None

def transformMethodDeclaration(context: Context, decl: MethodDeclaration) =
  val methodBody = decl.getBody.toScala.map(body =>
    ast.Block(body.getStatements.asScala.map(transformStatement.curried(context)).toList)
  )
  val methodParameters = decl.getParameters.asScala.map(transformParameter).toList
  val methodModifiers = decl.getModifiers.asScala.map(transformModifier).toList

  if (context.isConstructor(decl.getName))
    ast.Constructor(
      parameters = methodParameters,
      body = methodBody,
      modifiers = methodModifiers
    )
  else
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

def transformExpression(context: Context, expr: Expression): ast.Expression =
  expr match
    case expr: VariableDeclarationExpr =>
      ast.VariableDeclarationList(
        expr.getVariables.asScala.map(transformDeclaratorToVariable.curried(context)).toList
      )
    case expr: LiteralExpr => transformLiteral(expr)
    case expr: ObjectCreationExpr => transformObjectCreationExpression(context, expr)
    case expr: BinaryExpr => transformBinaryExpression(context, expr)
    case expr: UnaryExpr => transformUnaryExpression(context, expr)
    case expr: EnclosedExpr => ast.ParenthesizedExpression(transformExpression(context, expr.getInner))
    case expr: NameExpr => transformNameInContext(context, expr.getName)
    case expr: AssignExpr => transformAssignExpression(context, expr)
    case expr: FieldAccessExpr => transformFieldAccessExpression(context, expr)
    case expr: ThisExpr => ast.ThisKeyword()
    case expr: MethodCallExpr => transformMethodCall(context, expr)
    case expr: ArrayCreationExpr => transformArrayCreationExpression(context, expr)
    case expr: ArrayAccessExpr => transformArrayAccessExpression(context, expr)
    case _ => throw new Error("not supported")

def transformArrayAccessExpression(context: Context, expr: ArrayAccessExpr) =
  ast.ElementAccessExpression(
    transformExpression(context, expr.getName),
    transformExpression(context, expr.getIndex)
  )

def transformArrayCreationExpression(context: Context, expr: ArrayCreationExpr) =
  ast.ArrayLiteralExpression(
    expr.getInitializer.orElseThrow().getValues.asScala.map(transformExpression.curried(context)).toList
  )

def transformMethodCall(context: Context, expr: MethodCallExpr) =
  val scope = expr.getScope.toScala
  val arguments = expr.getArguments.asScala.map(transformExpression.curried(context)).toList
  if (scope.isEmpty)
    ast.CallExpression(
      transformNameInContext(context, expr.getName),
      arguments
    )
  else
    ast.CallExpression(
      ast.PropertyAccessExpression(
        transformExpression(context, scope.get),
        transformName(expr.getName)
      ),
      arguments
    )

def transformNameInContext(context: Context, name: SimpleName) =
  if (context.isMember(name))
    ast.PropertyAccessExpression(
      ast.ThisKeyword(),
      transformName(name)
    )
  else
    transformName(name)

def transformFieldAccessExpression(context: Context, expr: FieldAccessExpr) =
  ast.PropertyAccessExpression(
    transformExpression(context, expr.getScope),
    transformName(expr.getName)
  )

def transformAssignExpression(context: Context, expr: AssignExpr) =
  ast.BinaryExpression(
    transformExpression(context, expr.getTarget),
    transformExpression(context, expr.getValue), ast.EqualsToken()
  )

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

def transformBinaryExpression(context: Context, expr: BinaryExpr): ast.BinaryExpression =
  ast.BinaryExpression(
    transformExpression(context, expr.getLeft),
    transformExpression(context, expr.getRight),
    transformOperator(expr.getOperator)
  )

def transformUnaryExpression(context: Context, expr: UnaryExpr): ast.PrefixUnaryExpression =
  ast.PrefixUnaryExpression(
    transformOperator(expr.getOperator).kind,
    transformExpression(context, expr.getExpression)
  )

def transformObjectCreationExpression(context: Context, expr: ObjectCreationExpr) =
  ast.NewExpression(
    ast.Identifier(expr.getType.getName.getIdentifier),
    transformArguments(context, expr.getArguments),
    transformTypeArguments(expr.getTypeArguments)
  )

def transformArguments(context: Context, expressions: NodeList[Expression]) =
  expressions.asScala.map(transformExpression.curried(context)).toList

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
    case _ => throw new Error("not supported")
