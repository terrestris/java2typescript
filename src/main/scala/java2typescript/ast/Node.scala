package de.terrestris
package java2typescript.ast

trait Node {
  val kind: Int
  val `flags`: Int = 0
}
