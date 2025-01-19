package java2typescript.transformer

import java2typescript.ast
import java2typescript.ast.MethodDeclaration

import scala.collection.mutable

def isStatic(declaration: MethodDeclaration) =
  declaration.modifiers.exists(m => m.kind == ast.SyntaxKind.StaticKeyword)

def isAbstract(declaration: MethodDeclaration) =
  declaration.modifiers.exists(m => m.kind == ast.SyntaxKind.AbstractKeyword)

def groupMethods(classContext: ClassContext, methods: List[ast.MethodDeclaration]): List[List[ast.MethodDeclaration]] =
  if (methods.isEmpty)
    return List()

  val groupedMethods = mutable.Buffer[List[ast.MethodDeclaration]]()
  var unprocessed = methods

  while (unprocessed.nonEmpty)
    val groupNonStatic = mutable.Buffer[ast.MethodDeclaration]()
    val groupStatic = mutable.Buffer[ast.MethodDeclaration]()
    if (isStatic(unprocessed.head))
      groupStatic.append(unprocessed.head)
    else
      groupNonStatic.append(unprocessed.head)
    val ungrouped = mutable.Buffer[ast.MethodDeclaration]()
    for (method <- unprocessed.tail)
      if (method.name == unprocessed.head.name)
        if (isStatic(method))
          groupStatic.append(method)
        else
          groupNonStatic.append(method)
      else
        ungrouped.append(method)

    if (groupStatic.nonEmpty)
      // we add non static overloads to all functions so we can call the static functions with this prefixed
      // this avoids having to do signature detection inside the code analysis step
      groupNonStatic.appendAll(
        groupStatic.map(createNonStaticOverloadForStaticMethod.curried(classContext))
      )
      groupedMethods.append(groupStatic.toList)

    if (groupNonStatic.length >= 2)
      // we have no solution for abstract overloads so we drop the abstract methods in the next steps
      groupedMethods.append(groupNonStatic.filter(m => !isAbstract(m)).toList)
    else
      groupedMethods.append(groupNonStatic.toList)

    unprocessed = ungrouped.toList

  groupedMethods.toList

def createNonStaticOverloadForStaticMethod(classContext: ClassContext, methodDeclaration: MethodDeclaration) =
  ast.MethodDeclaration(
    methodDeclaration.name,
    methodDeclaration.`type`,
    methodDeclaration.parameters,
    methodDeclaration.typeParameters,
    Some(
      ast.Block(
        List(
          ast.ReturnStatement(
            Some(
              ast.CallExpression(
                ast.PropertyAccessExpression(
                  transformName(classContext.classOrInterface.get.getName),
                  methodDeclaration.name
                ),
                methodDeclaration.parameters.map(m => m.name)
              )
            )
          )
        )
      )
    ),
    methodDeclaration.modifiers.filter(m => m.kind != ast.SyntaxKind.StaticKeyword)
  )

def createMethodOverloads(methods: List[ast.MethodDeclaration]): List[ast.MethodDeclaration] =
  methods.map(removeBody) ::: List(
    createOverloadFunction(methods)
  )

def createConstructorOverloads(methods: List[ast.Constructor]): List[ast.Constructor] =
  methods.map(removeBody) ::: List(
    createOverloadFunction(methods)
  )

def createArgsParameter =
  List(ast.Parameter(
    name = ast.Identifier("args"),
    dotDotDotToken = Some(ast.DotDotDotToken()),
    `type` = Some(ast.ArrayType(ast.AnyKeyword()))
  ))

def createOverloadFunction(constructors: List[ast.Constructor]): ast.Constructor =
  val ps = createArgsParameter
  val b = Some(ast.Block(
    constructors.map(m => createSignatureIfBlock(m.parameters, m.body.get))
      ::: List(
      createNoSignatureMatchStatement()
    )
  ))

  ast.Constructor(
    parameters = ps,
    body = b,
    modifiers = constructors.head.modifiers
  )

def createOverloadFunction(methods: List[ast.MethodDeclaration]): ast.MethodDeclaration =
  val ps = createArgsParameter
  val b = Some(ast.Block(
    methods.map(m => createSignatureIfBlock(m.parameters, m.body.get))
      ::: List(
      createNoSignatureMatchStatement()
    )
  ))
  ast.MethodDeclaration(
    name = methods.head.name,
    `type` = methods.head.`type`,
    typeParameters = methods.head.typeParameters,
    parameters = ps,
    body = b,
    modifiers = methods.head.modifiers
  )

def createSignatureIfBlock(parameters: List[ast.Parameter], body: ast.Block): ast.IfStatement =
  ast.IfStatement(
    andExpressions(
      List(ast.BinaryExpression(
        ast.PropertyAccessExpression(
          ast.Identifier("args"),
          ast.Identifier("length")
        ),
        ast.NumericLiteral(parameters.length.toString),
        ast.EqualsEqualsEqualsToken()
      ))
        ::: parameters.zipWithIndex.map {
        (param, index) =>
          createParameterTypeCheck(ast.ElementAccessExpression(
            ast.Identifier("args"),
            ast.NumericLiteral(index.toString)
          ), param.`type`.get)
      }
    ),
    ast.Block(
      parameters.zipWithIndex.map(createParameterIntializer)
        ::: body.statements
    )
  )

def andExpressions(expressions: List[ast.Expression]): ast.Expression =
  expressions.tail.foldLeft(expressions.head) {
    (left, right) => ast.BinaryExpression(left, right, ast.AmpersandAmpersandToken())
  }

def createTypeOf(expr: ast.Expression, `type`: String) =
  ast.BinaryExpression(
    ast.TypeOfExpression(expr),
    ast.StringLiteral(`type`),
    ast.EqualsEqualsEqualsToken()
  )

def createParameterTypeCheck(expr: ast.Expression, `type`: ast.Type): ast.Expression =
  `type` match
    case t: ast.NumberKeyword => createTypeOf(expr, "number")
    case t: ast.StringKeyword => createTypeOf(expr, "string")
    case t: ast.BooleanKeyword => createTypeOf(expr, "boolean")
    case t: ast.ArrayType => ast.CallExpression(ast.PropertyAccessExpression(ast.Identifier("Array"), ast.Identifier("isArray")), List(expr))
    case t: ast.TypeReference => ast.BinaryExpression(
      expr,
      t.typeName,
      ast.InstanceOfKeyword()
    )

def createParameterIntializer(param: ast.Parameter, num: Int): ast.Statement =
  ast.VariableStatement(
    ast.VariableDeclarationList(List(
      ast.VariableDeclaration(
        name=param.name,
        `type`=param.`type`,
        initializer=Some(ast.ElementAccessExpression(
          ast.Identifier("args"),
          ast.NumericLiteral(num.toString)
        ))
      )
    ))
  )

def createNoSignatureMatchStatement(): ast.Statement =
  ast.ThrowStatement(
    ast.NewExpression(
      ast.Identifier("Error"),
      List(ast.StringLiteral("overload does not exist"))
    )
  )

def removeBody(method: ast.MethodDeclaration): ast.MethodDeclaration =
  ast.MethodDeclaration(
    method.name,
    method.`type`,
    method.parameters,
    method.typeParameters,
    None,
    method.modifiers
  )

def removeBody(constructor: ast.Constructor): ast.Constructor =
  ast.Constructor(
    parameters = constructor.parameters,
    body = None,
    modifiers = constructor.modifiers
  )
