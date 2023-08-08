package de.terrestris
package java2typescript

import java2typescript.util.resolveImportPath

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class ImportPathSpec extends AnyFlatSpec with should.Matchers {
  "the import path resolver" should "resolve path without package" in {
    resolveImportPath(Array(""), Array("a", "b")) should be("a/b")
  }

  "the import path resolver" should "resolve path with longer package then import" in {
    resolveImportPath(Array("a", "b", "c"), Array("a", "b")) should be("..")
  }

  "the import path resolver" should "resolve path with fully matching package" in {
    resolveImportPath(Array("a", "b"), Array("a", "b", "c", "d")) should be("c/d")
  }

  "the import path resolver" should "resolve path with partially matching package" in {
    resolveImportPath(Array("a", "b", "f", "g"), Array("a", "b", "c", "d")) should be("../../c/d")
  }
}
