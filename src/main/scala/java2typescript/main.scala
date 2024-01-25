package java2typescript

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import java.io.File
import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.CollectionConverters.*

@main def main(configFile: String, files: String*): Unit =
  val configPath = Paths.get(configFile).toAbsolutePath.normalize

  val config = parseConfig(readFile(configPath.toFile))

  val sourcePath = configPath.getParent.resolve(config.source).toAbsolutePath.normalize
  val targetPath = configPath.getParent.resolve(config.target).toAbsolutePath.normalize

  if (files.isEmpty)
    val (success, total) = walkDirectory(config, sourcePath, targetPath)
    println(s"processed $success files of $total successfully")
  else
    for (file <- files)
      val sourceFile = Paths.get(file).toAbsolutePath.normalize
      val targetFile = targetPath.resolve(sourcePath.relativize(sourceFile))
      handleFile(config, sourceFile, targetFile)

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
  val fileWriter = new java.io.PrintWriter(changeFileExtension(target, "ts").toFile)
  try
    fileWriter.write(tsContent)
  finally
    fileWriter.close()

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
