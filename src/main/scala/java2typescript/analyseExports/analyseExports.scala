package java2typescript.analyseExports

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.{ClassOrInterfaceDeclaration, EnumDeclaration, TypeDeclaration}
import java2typescript.{Config, ast, transformer}

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

def analyseExports(code: String): List[ImportMapping] = {
  val cu = StaticJavaParser.parse(code)
  parseCompilationUnit(cu)
}

def parseCompilationUnit(cu: CompilationUnit): List[ImportMapping] =
  val packageName = cu.getPackageDeclaration.map(d => d.getName.asString()).toScala
  cu.getTypes.asScala.flatMap(t => parseTypeDeclaration(packageName, t)).toList

def parseTypeDeclaration(packageName: Option[String], decl: TypeDeclaration[?]) =
  decl match
    case decl: ClassOrInterfaceDeclaration => parseDeclaration(packageName, decl)
    case decl: EnumDeclaration => parseDeclaration(packageName, decl)
    case _ => throw new Error("not supported")

def parseDeclaration(
  packageName: Option[String],
  decl: ClassOrInterfaceDeclaration|EnumDeclaration
): List[ImportMapping] =
  val name = decl.getName.asString
  List(ImportMapping(
    packageName,
    name,
    name
  ))
