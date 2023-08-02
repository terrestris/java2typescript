package de.terrestris
package java2typescript.transformer.statements

import java2typescript.ast.*

def transformStatement(statement: com.github.javaparser.ast.stmt.BlockStmt): Statement = {
  VariableStatement(
    VariableDeclarationList(
      List(
        VariableDeclaration(
          Identifier(
            "s"
          ),
          StringKeyword(),
          StringLiteral("abc")
        )
      )
    )
  )
}
