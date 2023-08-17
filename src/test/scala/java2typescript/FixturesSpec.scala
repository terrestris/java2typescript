package de.terrestris
package java2typescript

import org.scalatest.Inspectors.forAll
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import scala.collection.mutable.ListBuffer
import scala.collection.BufferedIterator
import scala.io.Source
import java2typescript.parser.{parse, parseMethodBody}
import java2typescript.writer.write

class Options(
  var debug: Boolean = false,
  var methodBody: Boolean = false
)

class Fixture(
  val title: String,
  val javaCode: String,
  val typeScriptCode: String,
  val options: Options
)

class FixtureCollection(
  val title: String,
  val fixtures: List[Fixture]
)

def dropWhile(it: BufferedIterator[String], condition: String => Boolean): Unit =
  while (condition(it.head))
    it.next()

def getCodeBlock(it: BufferedIterator[String], syntax: String): String = {
  dropWhile(it, l => !l.startsWith(s"```$syntax"))
  it.next()
  val codeLines = ListBuffer[String]()
  var line = it.next()
  while (!line.startsWith("```")) {
    codeLines.append(line)
    line = it.next()
  }
  codeLines.mkString("\n") + "\n"
}

def getPrefixed(it: BufferedIterator[String], prefix: String): String = {
  dropWhile(it, l => !l.startsWith(prefix))
  val line = it.next()
  line.substring(prefix.length)
}

def getOptions(it: BufferedIterator[String]): Options = {
  dropWhile(it, l => l.isBlank)
  if (!it.head.startsWith("options:"))
    return Options()
  val line = it.next()
  val pattern = "options:(?:\\s*(\\w+)\\s*,?)*".r
  val options = Options()
  val patternMatch = pattern.findFirstMatchIn(line)
  for group <- patternMatch.get.subgroups do
    group match {
      case "debug" => options.debug = true
      case "methodBody" => options.methodBody = true
      case s: String => throw new Error(s"unrecognized option $s")
    }
  options
}

def getFixture(it: BufferedIterator[String]): Fixture = {
  val title = getPrefixed(it, "## ")
  val options = getOptions(it)
  val javaCode = getCodeBlock(it, "java")
  val typescriptCode = getCodeBlock(it, "typescript")
  Fixture(title, javaCode, typescriptCode, options)
}

def getFixtureCollection(it: BufferedIterator[String]): FixtureCollection = {
  val collectionTitle = getPrefixed(it, "# ")
  val fixtureList = ListBuffer[Fixture]()
  while (it.hasNext)
    fixtureList.append(getFixture(it))
  FixtureCollection(collectionTitle, fixtureList.toList)
}

def fixtures(): List[FixtureCollection] = {
  val fixturesDir = getClass.getResource("/fixtures/")
  val folder = File(fixturesDir.getPath)
  if (!folder.exists || !folder.isDirectory)
    return List()
  val fixColls = folder.listFiles
    .filter(f => f.getName.endsWith(".md"))
    .map(f => {
      val source = Source.fromFile(f)
      val collection = getFixtureCollection(source.getLines.buffered)
      source.close()
      println(s"${collection.fixtures.length} fixtures in file $f")
      collection
    })
    .toList
  val anyDebug = fixColls.exists(coll => coll.fixtures.exists(f => f.options.debug))
  if (anyDebug)
    println("Found fixtures with debug option. Only running those fixtures.")
    fixColls
      .filter(coll => coll.fixtures.exists(f => f.options.debug))
      .map(coll => FixtureCollection(coll.title, coll.fixtures.filter(f => f.options.debug)))
  else
    fixColls
}

class FixturesSpec extends AnyFunSpec with Matchers {
  forAll(fixtures())(
    collection => {
      describe(collection.title) {
        forAll(collection.fixtures) {
          fix => {
            it(fix.title) {
              val parsed = if (fix.options.methodBody)
                parseMethodBody(wrapStatementJava(fix.javaCode))
              else
                parse(fix.javaCode)

              val written = write(parsed)
              written should be(fix.typeScriptCode)
              if (fix.options.debug)
                throw new Error("Fixture has debug option enabled")
            }
          }
        }
      }
    }
  )
}
