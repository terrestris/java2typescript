package java2typescript.transformer

import com.github.javaparser.ast.Modifier.Keyword
import com.github.javaparser.ast.body.{BodyDeclaration, EnumDeclaration, FieldDeclaration, MethodDeclaration}
import java2typescript.ast

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

def transformEnumDeclaration(
  context: FileContext|ClassContext,
  decl: EnumDeclaration,
  additionalModifiers: List[ast.Modifier] = List()
): List[ast.Statement] =
  val enumMembers = decl.getEntries.asScala.map(e => ast.EnumMember(transformName(e.getName))).toList
  val classContext = context match
    case c: FileContext => ClassContext(c, Some(decl))
    case c: ClassContext => ClassContext(c, Some(decl), Option(c))

  val otherMembers = decl.getMembers.asScala
    .flatMap(transformOtherEnumMember.curried(classContext))

  List(ast.EnumDeclaration(
    transformName(decl.getName),
    members = enumMembers,
    additionalModifiers
  )) ::: otherMembers.toList

def transformOtherEnumMember(context: ClassContext, member: BodyDeclaration[?]): List[ast.Statement] =
  member match
    case member: FieldDeclaration =>
      member.getVariables.asScala.toList.map(declarator =>
        ast.VariableStatement(ast.VariableDeclarationList(List(transformDeclaratorToVariable(context, declarator))))
      )
    case member: MethodDeclaration =>
      List(transformEnumMethodDeclaration(context, member))

def transformEnumMethodDeclaration(context: ClassContext, decl: MethodDeclaration): ast.Statement =
  val methodParameters = decl.getParameters.asScala.map(transformParameter.curried(context)).toList
  val methodContext = ParameterContext(context, methodParameters.toBuffer)
  val methodBody = decl.getBody.toScala.map(body =>
    ast.Block(body.getStatements.asScala.map(transformStatement.curried(methodContext)).toList)
  )
  val methodModifiers = if (decl.getModifiers.asScala.exists(m => m.getKeyword == Keyword.PUBLIC))
    List(ast.ExportKeyword())
  else
    List()

  ast.FunctionDeclaration(
    transformName(decl.getName),
    `type` = transformType(context, decl.getType),
    typeParameters = decl.getTypeParameters.asScala.map(transformType.curried(context)).map {
      t => t.get
    }.toList,
    parameters = methodParameters,
    body = methodBody,
    modifiers = methodModifiers
  )

