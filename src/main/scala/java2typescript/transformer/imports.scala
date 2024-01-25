package java2typescript.transformer

import com.github.javaparser.ast.{ImportDeclaration, PackageDeclaration}
import java2typescript.{Config, ast}

import java.util.Optional

def transformImport(config: Config, pack: Optional[PackageDeclaration], importDeclaration: ImportDeclaration) = {
  val importName = importDeclaration.getName.toString
  val identifier = importDeclaration.getName.getIdentifier
  config.imports
    .flatMap {
      _.find(loc => loc.`class` == importName)
    }
    .map {
      importConfig =>
        ast.ImportDeclaration(
          ast.ImportClause(
            ast.NamedImports(
              List(
                ast.ImportSpecifier(
                  ast.Identifier(identifier)
                )
              )
            )
          ),
          ast.StringLiteral(importConfig.location)
        )
    }
    .getOrElse {
      val packagePath = pack.map(p => p.getName.toString.split("\\.")).orElse(Array[String]())
      val importPath = importDeclaration.getName.getQualifier.map(q => q.toString.split("\\.")).orElse(Array[String]())
      val resolvedPath = resolveImportPath(packagePath, importPath)

      ast.ImportDeclaration(
        ast.ImportClause(
          ast.NamedImports(
            List(
              ast.ImportSpecifier(
                ast.Identifier(identifier)
              )
            )
          )
        ),
        ast.StringLiteral(
          s"$resolvedPath/$identifier.ts"
        )
      )
    }
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
