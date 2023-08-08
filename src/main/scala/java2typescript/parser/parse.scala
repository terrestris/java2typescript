package de.terrestris.java2typescript
package parser

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.MethodDeclaration
import scala.jdk.CollectionConverters.*

import java.util.Optional

def parseMethodBody(code: String): List[ast.Node] = {
  val body = StaticJavaParser.parse(code)
    .findFirst(classOf[MethodDeclaration])
    .orElseThrow()
    .getBody
    .orElseThrow()

  transformer.transformBlockStatement(body)
}

def parse(code: String): List[ast.Node] = {
  val cu = StaticJavaParser.parse(code)

  transformer.transformCompilationUnit(cu)
}
