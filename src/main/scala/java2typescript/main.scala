package de.terrestris.java2typescript

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import java.io.File
import java.nio.file.{Files, Path, Paths}
import scala.io.Source

@main def main(configFile: String): Unit = {
  val mapper = JsonMapper.builder()
    .addModule(DefaultScalaModule)
    .build()

  val configPath = Paths.get(configFile).toAbsolutePath.normalize

  val config = mapper.readValue(readFile(configPath.toFile), classOf[Config])

  val sourcePath = configPath.getParent.resolve(config.source)
  val targetPath = configPath.getParent.resolve(config.target)

  walkDirectory(sourcePath, targetPath)
}

def readFile(file: File): String = {
  val bufferedSource = Source.fromFile(file)
  val content = bufferedSource.getLines.mkString("\n")
  bufferedSource.close
  content
}

def handleFile(source: Path, target: Path): Unit = {
  target.getParent.toFile.mkdirs()
  val javaContent = readFile(source.toFile)
  val parseResult = try
    parser.parse(javaContent)
  catch
    case e: Error =>
      throw new Error(s"Error parsing File: $target", e)
  val tsContent = try
    writer.write(parseResult)
  catch
    case e: Error =>
      throw new Error(s"Error writing File: $target", e)
  val p = new java.io.PrintWriter(target.toFile)
  try
    p.write(tsContent)
  finally
    p.close()
}

def walkDirectory(source: Path, target: Path): Unit = {
  Files.newDirectoryStream(source)
    .forEach(sourceFile => {
      val targetFile = target.resolve(source.relativize(sourceFile))
      if (sourceFile.toFile.isDirectory)
        walkDirectory(sourceFile, targetFile)
      else
        handleFile(sourceFile, targetFile)
    })
}
