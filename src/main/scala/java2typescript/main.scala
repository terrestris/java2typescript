package de.terrestris.java2typescript

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import java.io.File
import java.nio.file.{Files, Path, Paths}
import scala.io.Source

@main def main(configFile: String): Unit = {
  val configPath = Paths.get(configFile).toAbsolutePath.normalize

  val config = parseConfig(readFile(configPath.toFile))

  val sourcePath = configPath.getParent.resolve(config.source)
  val targetPath = configPath.getParent.resolve(config.target)

  walkDirectory(config, sourcePath, targetPath)
}

def parseConfig(config: String): Config = {
  val mapper = JsonMapper.builder()
    .addModule(DefaultScalaModule)
    .build()

  mapper.readValue(config, classOf[Config])
}

def readFile(file: File): String = {
  val bufferedSource = Source.fromFile(file)
  val content = bufferedSource.getLines.mkString("\n")
  bufferedSource.close
  content
}

def handleFile(config: Config, source: Path, target: Path): Unit = {
  println(source.toFile)
  target.getParent.toFile.mkdirs()
  val javaContent = readFile(source.toFile)
  val parseResult = try
    parser.parse(config, javaContent)
  catch
    case e: Error => throw new Error(s"Error parsing java code from: $source", e)
  val tsContent = try
    writer.write(parseResult)
  catch
    case e: Error => throw new Error(s"Error writing typescript code for: $source", e)
  val p = new java.io.PrintWriter(target.toFile)
  try
    p.write(tsContent)
  finally
    p.close()
}

def walkDirectory(config: Config, source: Path, target: Path): Unit = {
  Files.newDirectoryStream(source)
    .forEach(sourceFile => {
      val targetFile = target.resolve(source.relativize(sourceFile))
      if (sourceFile.toFile.isDirectory)
        walkDirectory(config, sourceFile, targetFile)
      else
        handleFile(config, sourceFile, targetFile)
    })
}
