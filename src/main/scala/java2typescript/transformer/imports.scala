package java2typescript.transformer

import com.github.javaparser.ast.{ImportDeclaration, PackageDeclaration}
import java2typescript.analyseExports.Import
import java2typescript.{Config, ast}

import java.util.Optional

def createImports(context: FileContext): List[ast.ImportDeclaration] =
  val packagePath = context.packageName.getOrElse("").split("\\.")

  context.neededImports.groupBy {
      im =>
        im.fixedPath.getOrElse {
          val importPath = im.packageName.getOrElse("").split("\\.")
          val location = resolveImportPath(packagePath, importPath)
          s"$location/${im.getTypescriptFile}"
        }
    }.map {
      im => transformImports(context.packageName, im._1, im._2.toList)
    }.toList

def transformImports(packageName: Option[String], path: String, importMappings: List[Import]): ast.ImportDeclaration = {
  val packagePath = packageName.getOrElse("").split("\\.")

  ast.ImportDeclaration(
    ast.ImportClause(
      ast.NamedImports(
        importMappings.map {
          im => ast.ImportSpecifier(
            ast.Identifier(im.getTypescriptImport)
          )
        }
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
