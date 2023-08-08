package de.terrestris.java2typescript
package util

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
