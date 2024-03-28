package java2typescript.analyseExports

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Modifier.Keyword
import com.github.javaparser.ast.body.{BodyDeclaration, ClassOrInterfaceDeclaration, EnumDeclaration, TypeDeclaration}

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

def analyseExports(code: String): List[Import] = {
  val cu = StaticJavaParser.parse(code)
  parseCompilationUnit(cu)
}

def parseCompilationUnit(cu: CompilationUnit): List[Import] =
  val packageName = cu.getPackageDeclaration.map(d => d.getName.asString()).toScala
  cu.getTypes.asScala.flatMap(t => parseTypeDeclaration(packageName, None, t)).toList

def parseTypeDeclaration(packageName: Option[String], scope: Option[String], decl: TypeDeclaration[?]) =
  decl match
    case decl: ClassOrInterfaceDeclaration => parseClassOrInterfaceDeclaration(packageName, scope, decl)
    case decl: EnumDeclaration => parseEnumDeclaration(packageName, scope, decl)
    case _ => throw new Error("not supported")

def parseEnumDeclaration(
  packageName: Option[String],
  scope: Option[String],
  decl: EnumDeclaration
): List[Import] =
  val name = decl.getName.asString
  val publicMembers = decl.getMembers.asScala.filter {
    m => m.isMethodDeclaration
  }.map {
    m => m.asMethodDeclaration
  }.filter {
    m => m.getModifiers.asScala.exists(mo => mo.getKeyword == Keyword.PUBLIC)
  }
  val nestedScope = addToScope(scope, name)
  List(Import(
    packageName,
    scope,
    name
  )) ::: publicMembers.map {
    m => Import(
      packageName,
      nestedScope,
      m.getNameAsString
    )
  }.toList

def parseClassOrInterfaceDeclaration(
  packageName: Option[String],
  scope: Option[String],
  decl: ClassOrInterfaceDeclaration
): List[Import] =
  val name = decl.getName.asString
  val newScope = addToScope(scope, name)
  List(Import(
    packageName,
    scope,
    name
  )) ::: decl.getMembers.asScala
    .flatMap(m => parseMember(packageName, newScope, m))
    .toList

def parseMember(
  packageName: Option[String],
  scope: Option[String],
  member: BodyDeclaration[?]
): List[Import] =
  if (member.isClassOrInterfaceDeclaration)
    parseClassOrInterfaceDeclaration(packageName, scope, member.asClassOrInterfaceDeclaration)
  else
    List()

def addToScope(scope: Option[String], newVal: String): Option[String] =
  scope match {
    case scopeVal: Some[String] => Some(s"${scopeVal.get}.$newVal")
    case None => Some(newVal)
  }
