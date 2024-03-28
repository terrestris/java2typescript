package java2typescript.parser

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.{ClassOrInterfaceDeclaration, MethodDeclaration}
import java2typescript.{Config, ast, transformer}
import java2typescript.transformer.{ClassContext, FileContext, ParameterContext, ProjectContext}

import scala.collection.mutable.ListBuffer

def parseMethodBody(code: String): List[ast.Node] = {
  val body = StaticJavaParser.parse(code)
    .findFirst(classOf[MethodDeclaration])
    .orElseThrow()
    .getBody
    .orElseThrow()
  val methodContext = ParameterContext(ClassContext(FileContext(ProjectContext(Config(), List()), None), None), ListBuffer())
  transformer.transformBlockStatement(methodContext, body).statements
}

def parse(context: ProjectContext, code: String): List[ast.Node] = {
  val cu = StaticJavaParser.parse(code)

  if (transformer.unsupported.checkUnsupported(code))
    transformer.unsupported.transformCompilationUnit(context, cu)
  else
    transformer.transformCompilationUnit(context, cu)
}
