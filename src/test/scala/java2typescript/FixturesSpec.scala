package java2typescript

import org.scalatest.Inspectors.forAll
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import scala.collection.mutable.ListBuffer
import scala.collection.BufferedIterator
import scala.io.Source

import java2typescript.analyseExports.analyseExports
import java2typescript.parser.{parse, parseMethodBody}
import java2typescript.transformer.ProjectContext
import java2typescript.writer.write

class Options(
  var debug: Boolean = false,
  var methodBody: Boolean = false,
  var skip: Boolean = false
)

class Fixture(
  val title: String,
  val javaCodes: List[String],
  val typeScriptCodes: List[String],
  val options: Options,
  val config: Option[Config]
)

class FixtureCollection(
  val title: String,
  val fixtures: List[Fixture]
)

def dropWhile(it: BufferedIterator[String], condition: String => Boolean): Unit =
  while (condition(it.head))
    it.next()

def getCodeBlock(it: BufferedIterator[String], syntax: String): Option[String] = {
  if (!it.hasNext)
    return None
  dropWhile(it, l => l.isEmpty)
  if (!it.head.startsWith(s"```$syntax"))
    return None
  it.next()
  val codeLines = ListBuffer[String]()
  var line = it.next()
  while (!line.startsWith("```")) {
    codeLines.append(line)
    line = it.next()
  }
  Some(codeLines.mkString("\n") + "\n")
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
  val options = Options()
  for option <- line.substring(8).trim.split("\\s*,\\s*") do
    option match {
      case "debug" => options.debug = true
      case "methodBody" => options.methodBody = true
      case "skip" => options.skip = true
      case s: String => if (!s.isBlank) throw new Error(s"unrecognized option $s")
    }
  options
}

def loop[T](func: () => Option[T]): List[T] = {
  func() match {
    case res: Some[T] =>
      List(res.get) ::: loop(func)
    case None =>
      List()
  }
}

def getFixture(it: BufferedIterator[String]): Fixture = {
  val title = getPrefixed(it, "## ")
  val options = getOptions(it)
  val config = getCodeBlock(it, "json").map(parseConfig)
  val javaCodes = loop(() => getCodeBlock(it, "java"))
  val typescriptCodes = loop(() => getCodeBlock(it, "typescript"))
  if (javaCodes.length != typescriptCodes.length)
    throw new Error("Amount of java code blocks does not match amount of typescript code blocks")
  Fixture(title, javaCodes, typescriptCodes, options, config)
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
          fix => if (!fix.options.skip)
            it(fix.title) {
              val config = fix.config.getOrElse(Config())
              val parsed = if (fix.options.methodBody)
                if (fix.javaCodes.length != 1)
                  throw new Error("Cannot parse more then one method body")
                val parsed = parseMethodBody(wrapStatementJava(fix.javaCodes.head))
                val written = write(parsed)
                written should be(fix.typeScriptCodes.head)
              else
                val importMappings = fix.javaCodes.flatMap(analyseExports) ::: config.customImports

                val context = ProjectContext(config, importMappings)

                val parseResults = fix.javaCodes.map {
                  code => parse(context, code)
                }

                val writeResults = parseResults.map(write)

                writeResults.zip(fix.typeScriptCodes).foreach {
                  (written, expected) =>
                    written should be(expected)
                }

              if (fix.options.debug)
                throw new Error("Fixture has debug option enabled")
            }
        }
      }
    }
  )
}
