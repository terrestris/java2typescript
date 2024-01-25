package java2typescript.parser

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.{ClassOrInterfaceDeclaration, MethodDeclaration}
import java2typescript.{Config, ast, transformer}
import java2typescript.transformer.Context

def parseMethodBody(code: String): List[ast.Node] = {
  val body = StaticJavaParser.parse(code)
    .findFirst(classOf[MethodDeclaration])
    .orElseThrow()
    .getBody
    .orElseThrow()

  transformer.transformBlockStatement(Context(ClassOrInterfaceDeclaration()), body).statements
}

def parse(config: Config, code: String): List[ast.Node] = {
  val cu = StaticJavaParser.parse(code)

  if (transformer.unsupported.checkUnsupported(code))
    transformer.unsupported.transformCompilationUnit(cu)
  else
    transformer.transformCompilationUnit(config, cu)
}
