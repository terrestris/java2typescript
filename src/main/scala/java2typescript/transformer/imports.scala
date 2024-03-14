package java2typescript.transformer

import com.github.javaparser.ast.{ImportDeclaration, PackageDeclaration}
import java2typescript.analyseExports.ImportMapping
import java2typescript.{Config, ast}

import java.util.Optional

def transformImport(packageName: Option[String], importMapping: ImportMapping): ast.ImportDeclaration = {
  val packagePath = packageName.getOrElse("").split("\\.")
  val path = importMapping.fixedPath.getOrElse {
    val importPath = importMapping.packageName.getOrElse("").split("\\.")
    val location = resolveImportPath(packagePath, importPath)
    s"$location/${importMapping.typescriptName}.ts"
  }

  ast.ImportDeclaration(
    ast.ImportClause(
      ast.NamedImports(
        List(
          ast.ImportSpecifier(
            ast.Identifier(importMapping.typescriptName)
          )
        )
      )
    ),
    ast.StringLiteral(
      path
    )
  )
}

def resolveImportPath(packagePath: Array[String], importPath: Array[String]): String = {
  val differing = packagePath
    .zipAll(importPath, "", "")
    .dropWhile((a, b) => a == b)

  (
    differing.map(pair => pair(0)).takeWhile(v => v != "").map(v => "..").toList
      :::
      differing.map(pair => pair(1)).takeWhile(v => v != "").toList
    ).mkString("/")
}
