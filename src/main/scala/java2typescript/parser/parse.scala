package de.terrestris.java2typescript.parser

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.MethodDeclaration

import de.terrestris.java2typescript.ast
import de.terrestris.java2typescript.transformer

def parseMethodBody(code: String): List[ast.Node] = {
  val body = StaticJavaParser.parse(code)
    .findFirst(classOf[MethodDeclaration])
    .orElseThrow()
    .getBody
    .orElseThrow()

  transformer.transformBlockStatement(body).statements
}

def parse(code: String): List[ast.Node] = {
  val cu = StaticJavaParser.parse(code)

  transformer.transformCompilationUnit(cu)
}
