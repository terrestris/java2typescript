package java2typescript.transformer

import com.github.javaparser.ast.body.{ClassOrInterfaceDeclaration, FieldDeclaration, MethodDeclaration}
import com.github.javaparser.ast.expr.SimpleName
import java2typescript.analyseExports.ImportMapping
import java2typescript.{Config, ast}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

class ProjectContext(
  val config: Config,
  val importMappings: List[ImportMapping]
) {
  def isImportableName(name: SimpleName): Boolean =
    importMappings.exists {
      im => im.javaQualifiedName == name.asString()
    }
}

class FileContext(
  val projectContext: ProjectContext,
  val packageName: Option[String],
  val extractedExport: mutable.Buffer[ImportMapping] = ListBuffer(),
  val neededImports: mutable.Buffer[ImportMapping] = ListBuffer()
) extends ProjectContext(projectContext.config, projectContext.importMappings) {
  def addExtractedExport(imp: ImportMapping): Unit =
    extractedExport += imp
  def addImportIfNeeded(name: SimpleName): Unit =
    if (isImportableName(name))
      val exists = neededImports.exists {
        im => im.javaQualifiedName == name.asString()
      }
      if (!exists) {
        neededImports += importMappings.find {
          im => im.javaQualifiedName == name.asString()
        }.get
      }
}

class ClassContext(
  val fileContext: FileContext,
  val classOrInterface: ClassOrInterfaceDeclaration,
  val parentClassContext: Option[ClassContext] = Option.empty,
  val extractedStatements: mutable.Buffer[ast.Statement] = ListBuffer(),
) extends FileContext(fileContext.projectContext, fileContext.packageName, fileContext.extractedExport, fileContext.neededImports) {
  def addExtractedStatements(sts: List[ast.Statement]): Unit =
    extractedStatements.appendAll(sts)

  override def isImportableName(name: SimpleName): Boolean =
    super.isImportableName(name) && classOrInterface.getName.asString != name.asString
}

class ParameterContext(
  val classContext: ClassContext,
  val parameters: mutable.Buffer[ast.Parameter]
) extends ClassContext(classContext.fileContext, classContext.classOrInterface, classContext.parentClassContext, classContext.extractedStatements) {
  def isNonStaticMember(name: SimpleName): Boolean =
    classOrInterface.getMembers.asScala
      .exists {
        case mem: FieldDeclaration =>
          !mem.isStatic && mem.getVariables.asScala.exists(v => v.getName == name)
        case mem: MethodDeclaration =>
          !mem.isStatic && mem.getName == name
        case _ => false
      }
      &&
      !parameters.exists(p => p.name.escapedText == name.getIdentifier)

  def isStaticMember(name: SimpleName): Boolean =
    classOrInterface.getMembers.asScala
      .exists {
        case mem: FieldDeclaration =>
          mem.isStatic && mem.getVariables.asScala.exists(v => v.getName == name)
        case mem: MethodDeclaration =>
          mem.isStatic && mem.getName == name
        case _ => false
      }
      &&
      !parameters.exists(p => p.name.escapedText == name.getIdentifier)
}
