package java2typescript

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import java2typescript.analyseExports.analyseExports
import java2typescript.transformer.ProjectContext

import java.io.File
import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.CollectionConverters.*
import scala.util.Using
import scala.util.matching.Regex

@main def main(configFile: String, files: String*): Unit =
  val configPath = Paths.get(configFile).toAbsolutePath.normalize

  val config = parseConfig(readFile(configPath.toFile))

  val sourcePath = configPath.getParent.resolve(config.source).toAbsolutePath.normalize
  val targetPath = configPath.getParent.resolve(config.target).toAbsolutePath.normalize

  println("gather files")

  val gatheredFiles = if (files.isEmpty)
    gatherFiles(config, sourcePath)
  else
    files.map {
      file => Paths.get(file).toAbsolutePath.normalize
    }.toList

  val amount = gatheredFiles.length
  println(s"gathered $amount files")

  println("read files")
  val contents = readFiles(config, gatheredFiles)

  println("analyse exports")
  val analyseProgress = Progress(contents.length)
  val importMappings = contents.flatMap {
    (file, content) =>
      analyseProgress.step()
      analyseExports(content)
  }

  val context = ProjectContext(config, importMappings)

  println("parse files")
  val parseProgress = Progress(contents.length)
  val parseResults = contents.flatMap {
    (file, content) =>
      parseProgress.step()
      try Some(file, parser.parse(context, content)) catch
        case err: Throwable =>
          parseProgress.clear()
          println(s"error occured in $file")
          err.printStackTrace()
          None
  }

  println("create typescript code")
  val tsProgress = Progress(parseResults.length)
  val tsContents = parseResults.flatMap {
    (file, parseResult) =>
      tsProgress.step()
      try Some(file, writer.write(parseResult)) catch
        case err: Throwable =>
          tsProgress.clear()
          println(s"error occurred in $file")
          err.printStackTrace()
          None
  }

  println("write files")
  val writeProgress = Progress(tsContents.length)
  tsContents.foreach {
    (file, tsContent) => {
      writeProgress.step()
      val target = changeFileExtension(targetPath.resolve(sourcePath.relativize(file)), "ts")
      val fileWriter = new java.io.PrintWriter(target.toFile)
      try
        fileWriter.write(tsContent)
      finally
        fileWriter.close()
    }
  }

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

def changeFileExtension(originalPath: Path, newExtension: String): Path = {
  // Get the file name without extension
  val fileName: String = originalPath.getFileName.toString
  val dotIndex: Int = fileName.lastIndexOf('.')

  if (dotIndex != -1) {
    // Remove the old extension
    val baseName: String = fileName.substring(0, dotIndex)

    // Append the new extension
    val newFileName: String = s"$baseName.$newExtension"

    // Create a new Path with the updated file name
    originalPath.resolveSibling(newFileName)
  } else {
    // File has no extension, simply append the new extension
    val newFileName: String = s"$fileName.$newExtension"
    originalPath.resolveSibling(newFileName)
  }
}

def gatherFiles(config: Config, source: Path): List[Path] =
  Files.newDirectoryStream(source)
    .asScala
    .toList
    .flatMap {
      filePath => {
        val file = filePath.toFile
        val fileName = file.toString
        if (file.isDirectory)
          gatherFiles(config, filePath)
        else if (config.skipFiles.exists(fr => Regex(fr).unanchored.matches(fileName)))
          println(s"SKIPPED: $fileName")
          List()
        else if (fileName.endsWith(".java"))
          println(s"GATHERED: $fileName")
          List(filePath)
        else
          List()
      }
    }

def readFiles(config: Config, files: List[Path]): List[(Path, String)] =
  files.map {
    file =>
      (file, replace(readFile(file.toFile), config.replacements))
  }

class Progress (val total: Int, val barLength: Int = 50) {
  var count = 0
  def step(): Unit = {
    val filled = (count.toFloat / total * barLength).toInt
    this.clear()
    print(s"[${"=".repeat(filled)}>${" ".repeat(barLength - filled - 1)}]")
    count += 1
    if (count == total)
      println("")
  }
  def clear(): Unit = {
    print("\r")
  }
}
