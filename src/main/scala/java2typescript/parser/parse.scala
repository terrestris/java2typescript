package de.terrestris.java2typescript
package parser

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.MethodDeclaration
import de.terrestris.java2typescript.ast.Statement
import scala.jdk.CollectionConverters.*

import java.util.Optional

def parse(code: String): List[Statement] = {
  val cu = StaticJavaParser.parse(code)
  val method = cu
    .findFirst(classOf[MethodDeclaration])

  val body = method.orElseThrow().getBody().orElseThrow()

  List(transformer.statements.transformStatement(body))
}
