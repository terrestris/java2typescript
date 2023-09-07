package de.terrestris.java2typescript

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import java.io.File
import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.CollectionConverters.*

@main def main(configFile: String): Unit =
  val configPath = Paths.get(configFile).toAbsolutePath.normalize

  val config = parseConfig(readFile(configPath.toFile))

  val sourcePath = configPath.getParent.resolve(config.source)
  val targetPath = configPath.getParent.resolve(config.target)

  val (success, total) = walkDirectory(config, sourcePath, targetPath)
  println(s"processed $success files of $total successfully")

def parseConfig(config: String): Config =
  val mapper = JsonMapper.builder()
    .addModule(DefaultScalaModule)
    .build()

  mapper.readValue(config, classOf[Config])

def readFile(file: File): String =
  val bufferedSource = Source.fromFile(file)
  val content = bufferedSource.getLines.mkString("\n")
  bufferedSource.close
  content

def replace(text: String, replacements: List[ReplacementConfig]) =
  replacements.foldLeft(text) {
    (text, replacement) => {
      text.replaceAll(replacement.pattern, replacement.replacement)
    }
  }

def handleFile(config: Config, source: Path, target: Path): Unit =
  println(source.toFile)
  target.getParent.toFile.mkdirs()
  val javaContent = replace(readFile(source.toFile), config.replacements)
  val parseResult = try
    parser.parse(config, javaContent)
  catch
    case e: Error => throw new Error(s"Error transforming java code to typescript ast from: $source", e)
  val tsContent = try
    writer.write(parseResult)
  catch
    case e: Error => throw new Error(s"Error writing typescript code for: $source", e)
  val p = new java.io.PrintWriter(target.toFile)
  try
    p.write(tsContent)
  finally
    p.close()

def walkDirectory(config: Config, source: Path, target: Path): (Int, Int) =
  Files.newDirectoryStream(source)
    .asScala
    .toList
    .map {
      sourceFile => {
        val targetFile = target.resolve(source.relativize(sourceFile))
        val fileName = sourceFile.toFile.toString
        if (sourceFile.toFile.isDirectory)
          walkDirectory(config, sourceFile, targetFile)
        else if (config.skipFiles.exists(f => fileName.endsWith(f)))
          println("INFO: File skipped.")
          (0, 1)
        else if (sourceFile.toFile.toString.endsWith(".java"))
          try
            handleFile(config, sourceFile, targetFile)
            (1, 1)
          catch
            case e: Error =>
              println(e)
              e.printStackTrace()
              (0, 1)
        else
          (0, 0)
      }
    }
    .reduce {
      (a, b) => (a._1 + b._1, a._2 + b._2)
    }
