package de.terrestris.java2typescript.transformer

import de.terrestris.java2typescript.ast

import scala.collection.mutable

def groupMethodsByName(methods: List[ast.MethodDeclaration]): List[List[ast.MethodDeclaration]] =
  if (methods.isEmpty)
    return List()
  var name = methods.head.name
  var current = mutable.Buffer[ast.MethodDeclaration](methods.head)
  val overall = mutable.Buffer[List[ast.MethodDeclaration]]()
  def appendAndFilter() =
    if (current.length > 1)
      overall.append(current.filter(m => !m.modifiers.exists(m => m.kind == ast.SyntaxKind.AbstractKeyword)).toList)
    else
      overall.append(current.toList)
  for (
    method <- methods.tail
  ) {
    if (method.name == name)
      current.append(method)
    else
      appendAndFilter()
      name = method.name
      current = mutable.Buffer(method)
  }
  appendAndFilter()
  overall.toList

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
    `type` = ast.ArrayType(ast.AnyKeyword())
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
          ), param.`type`)
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
    case t: ast.ArrayType => createTypeOf(expr, "array")
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
