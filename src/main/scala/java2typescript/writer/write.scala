package de.terrestris
package java2typescript.writer

import java2typescript.ast.Statement

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.apigee.trireme.core.{NodeEnvironment, NodeScript, ScriptStatus}

import java.io.{BufferedReader, BufferedWriter, File, InputStreamReader, OutputStreamWriter}
import java.nio.charset.Charset
import scala.io.Source

def write(statements: List[Statement]): String = {
  val mapper = JsonMapper.builder()
    .addModule(DefaultScalaModule)
    .enable(SerializationFeature.INDENT_OUTPUT)
    .build()

  val serialized = mapper.writeValueAsString(statements)

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
