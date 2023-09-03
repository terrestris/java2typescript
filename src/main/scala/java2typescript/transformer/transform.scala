package de.terrestris.java2typescript.transformer

import com.github.javaparser.ast.Modifier.Keyword
import com.github.javaparser.ast.{CompilationUnit, ImportDeclaration, Modifier, NodeList, PackageDeclaration}
import com.github.javaparser.ast.`type`.{ArrayType, ClassOrInterfaceType, PrimitiveType, Type, VoidType}
import com.github.javaparser.ast.body.{BodyDeclaration, ClassOrInterfaceDeclaration, ConstructorDeclaration, FieldDeclaration, MethodDeclaration, Parameter, TypeDeclaration, VariableDeclarator}
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.stmt.{BlockStmt, BreakStmt, ContinueStmt, ExpressionStmt, ForStmt, IfStmt, ReturnStmt, Statement, ThrowStmt, WhileStmt}
import de.terrestris.java2typescript.{Config, ast}
import de.terrestris.java2typescript.ast.SyntaxKind
import de.terrestris.java2typescript.util.resolveImportPath

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

def transformImport(config: Config, pack: Optional[PackageDeclaration], importDeclaration: ImportDeclaration) = {
  val importName = importDeclaration.getName.toString
  val identifier = importDeclaration.getName.getIdentifier
  config.imports
    .flatMap {
      _.find(loc => loc.`class` == importName)
    }
    .map {
      importConfig =>
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
          ast.StringLiteral(importConfig.location)
        )
    }
    .getOrElse {
      val packagePath = pack.map(p => p.getName.toString.split("\\.")).orElse(Array[String]())
      val importPath = importDeclaration.getName.getQualifier.map(q => q.toString.split("\\.")).orElse(Array[String]())
      val resolvedPath = resolveImportPath(packagePath, importPath)
      
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
    case stmt: ThrowStmt => ast.ThrowStatement(transformExpression(context, stmt.getExpression))
    case stmt: WhileStmt => ast.WhileStatement(
      transformExpression(context, stmt.getCondition),
      transformStatement(context, stmt.getBody)
    )
    case stmt: ForStmt => transformForStatment(context, stmt)
    case stmt: BreakStmt => ast.BreakStatement()
    case stmt: ContinueStmt => ast.ContinueStatement()
    case _ => throw new Error("statement type not supported")

def transformForStatment(context: Context, stmt: ForStmt) = {
  val init = stmt.getInitialization.asScala
  if (init.length > 1)
    throw new Error("only one initializer for for loop supported")
  val transformedInit =
    if (init.nonEmpty)
      if (!init.head.isInstanceOf[VariableDeclarationExpr])
        throw new Error("only variable declaration expressions are supported in initializer")
      else
        Some(transformExpression(context, init.head))
    else
      None
  val update = stmt.getUpdate.asScala
  if (update.length > 1)
    throw new Error("only one updater for for loop supported")
  val transformedUpdate =
    if (update.nonEmpty)
      Some(transformExpression(context, update.head))
    else
      None
  ast.ForStatement(
    initializer = transformedInit,
    condition = stmt.getCompare.toScala.map(transformExpression.curried(context)),
    incrementor = transformedUpdate,
    statement = transformStatement(context, stmt.getBody)
  )
}

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
  val memberVals = decl.getMembers.asScala.flatMap(transformMember.curried(context))

  if (decl.isInterface)
    ast.InterfaceDeclaration(transformName(decl.getName), members = memberVals.toList, modifiers = additionalModifiers)
    ::
    context.internalClasses.toList
  else {
    val properties = memberVals
      .collect {
        case p: ast.PropertyDeclaration => p
      }
      .toList

    val constructors = memberVals
      .collect {
        case c: ast.Constructor => c
      }
      .toList

    val constructorsWithOverloads =
      if (constructors.length > 1)
        createConstructorOverloads(constructors)
      else
        constructors

    val methodsWithOverloads = groupMethodsByName(memberVals
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
      modifiers = additionalModifiers
    )
    ::
    context.internalClasses.toList
  }

def transformMember(context: Context, member: BodyDeclaration[?]): Option[ast.Member] =
  member match
    case member: FieldDeclaration =>
      val variables = member.getVariables.asScala.toList
      if (variables.length != 1)
        throw new Error(s"amount of variables in member not supported (${variables.length})")
      Some(transformDeclaratorToProperty(context, variables.head, member.getModifiers.asScala.toList))
    case member: MethodDeclaration =>
      Some(transformMethodDeclaration(context, member))
    case member: ConstructorDeclaration
      => Some(transformConstructorDeclaration(context, member))
    case member: ClassOrInterfaceDeclaration =>
      context.internalClasses.appendAll(transformClassOrInterfaceDeclaration(member))
      None

def transformConstructorDeclaration(context: Context, declaration: ConstructorDeclaration) =
  val methodBody = ast.Block(
    declaration.getBody.getStatements.asScala.map(transformStatement.curried(context)).toList
  )
  val methodParameters = declaration.getParameters.asScala.map(transformParameter).toList
  val methodModifiers = declaration.getModifiers.asScala.flatMap(transformModifier).toList

  ast.Constructor(
    parameters = methodParameters,
    body = Some(methodBody),
    modifiers = methodModifiers
  )

def transformMethodDeclaration(context: Context, decl: MethodDeclaration) =
  val methodBody = decl.getBody.toScala.map(body =>
    ast.Block(body.getStatements.asScala.map(transformStatement.curried(context)).toList)
  )
  val methodParameters = decl.getParameters.asScala.map(transformParameter).toList
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
    case expr: CastExpr => transformCastExpression(context, expr)
    case _ => throw new Error("not supported")

def transformCastExpression(context: Context, expr: CastExpr) = {
  val `type` = transformType(expr.getType)
  val castExpression = transformExpression(context, expr.getExpression)
  if (`type`.kind == SyntaxKind.NumberKeyword)
    ast.CallExpression(
      ast.PropertyAccessExpression(
        ast.Identifier("Math"),
        ast.Identifier("floor")
      ),
      List(castExpression)
    )
  else
    throw new Error("not supported")
}

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
    transformExpression(context, expr.getValue),
    if (expr.getOperator.name == "ASSIGN")
      transformOperator("ASSIGN")
    else
      transformOperator(s"${expr.getOperator.name}_EQUALS")
  )

def transformOperator(name: String): ast.Token =
  name match
    case "ASSIGN" => ast.EqualsToken()
    case "PLUS" => ast.PlusToken()
    case "MINUS" => ast.MinusToken()
    case "MULTIPLY" => ast.AsteriskToken()
    case "DIVIDE" => ast.SlashToken()
    case "AND" => ast.AmpersandAmpersandToken()
    case "OR" => ast.BarBarToken()
    case "EQUALS" => ast.EqualsEqualsEqualsToken()
    case "PLUS_EQUALS" => ast.PlusEqualsToken()
    case "MINUS_EQUALS" => ast.MinusEqualsToken()
    case "NOT_EQUALS" => ast.ExclamationEqualsEqualsToken()
    case "LESS_EQUALS" => ast.LessThanEqualsToken()
    case "LESS" => ast.LessThanToken()
    case "GREATER" => ast.GreaterThanToken()
    case "GREATER_EQUALS" => ast.GreaterThanEqualsToken()
    case "LOGICAL_COMPLEMENT" => ast.ExclamationToken()
    case "POSTFIX_INCREMENT"|"PREFIX_INCREMENT" => ast.PlusPlusToken()
    case "POSTFIX_DECREMENT"|"PREFIX_DECREMENT" => ast.MinusMinusToken()
    case _ => throw new Error("not supported")

def transformBinaryExpression(context: Context, expr: BinaryExpr): ast.BinaryExpression =
  ast.BinaryExpression(
    transformExpression(context, expr.getLeft),
    transformExpression(context, expr.getRight),
    transformOperator(expr.getOperator.name)
  )

def transformUnaryExpression(context: Context, expr: UnaryExpr): ast.PrefixUnaryExpression|ast.PostfixUnaryExpression =
  if (expr.getOperator.isPrefix)
    ast.PrefixUnaryExpression(
      transformOperator(expr.getOperator.name).kind.kind,
      transformExpression(context, expr.getExpression)
    )
  else
    ast.PostfixUnaryExpression(
      transformOperator(expr.getOperator.name).kind.kind,
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
