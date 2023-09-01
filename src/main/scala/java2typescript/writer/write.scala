package de.terrestris.java2typescript.writer

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import de.terrestris.java2typescript.ast

import java.io.{BufferedReader, BufferedWriter, File, InputStreamReader, OutputStreamWriter}
import java.nio.charset.Charset
import scala.io.Source

def serialize(statements: List[ast.Node]) = {
  val mapper = JsonMapper.builder()
    .addModule(DefaultScalaModule)
    .enable(SerializationFeature.INDENT_OUTPUT)
    .build()

  mapper.setSerializationInclusion(Include.NON_ABSENT)

  mapper.writeValueAsString(statements)
}

def write(statements: List[ast.Node]): String = {
  val serialized: String = serialize(statements)

  val fileName = "src/main/javascript/dist/index.js"

  val processBuilder = ProcessBuilder()
  processBuilder.redirectErrorStream(true)

  processBuilder.command("node", fileName)

  val process = processBuilder.start()

  val inp = BufferedWriter(OutputStreamWriter(process.getOutputStream))

  inp.write(serialized)
  inp.close()

  process.waitFor()

  Source.fromInputStream(process.getInputStream).mkString
}
