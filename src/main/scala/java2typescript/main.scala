package de.terrestris.java2typescript

import writer.write
import parser.parse

@main def main() = {
  println(write(parse("class A {}")))
}
