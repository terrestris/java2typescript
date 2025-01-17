package java2typescript.transformer

import com.github.javaparser.ast.`type`.ClassOrInterfaceType
import com.github.javaparser.ast.body.{ClassOrInterfaceDeclaration, EnumDeclaration, FieldDeclaration, MethodDeclaration}
import com.github.javaparser.ast.expr.SimpleName
import java2typescript.analyseExports.Import
import java2typescript.{Config, ast}

import scala.util.matching.Regex
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

class ProjectContext(
  val config: Config,
  val importMappings: List[Import]
) {
  def isImportable(scope: Option[String], name: String): Boolean =
    importMappings.exists {
      im => im.javaScope == scope && im.javaName == name
    }
}

class FileContext(
  val projectContext: ProjectContext,
  val packageName: Option[String],
  val extractedExport: mutable.Buffer[Import] = ListBuffer(),
  val neededImports: mutable.Buffer[Import] = ListBuffer(),
  val localIdentifiers: mutable.Buffer[ast.Identifier] = ListBuffer()
) extends ProjectContext(projectContext.config, projectContext.importMappings) {
  def addLocalName(identifier: ast.Identifier): Unit =
    localIdentifiers += identifier

  def isLocal(name: SimpleName): Boolean =
    localIdentifiers.exists(i => i.escapedText == name.getIdentifier)

  def addExtractedExport(imp: Import): Unit =
    extractedExport += imp

  def addImportIfNeeded(scope: Option[String], name: String): Unit =
    if (isImportable(scope, name))
      val exists = neededImports.exists {
        im => im.javaScope == scope && im.javaName == name
      }
      if (!exists) {
        neededImports += getImport(scope, name).get
      }
    else if (scope.isDefined)
      val splitted = scope.get.split('.')
      if (splitted.length == 1)
        addImportIfNeeded(None, splitted(0))
      else
        addImportIfNeeded(Some(splitted.dropRight(1).mkString(".")), splitted(-1))

  def getImport(scope: Option[String], name: String): Option[Import] =
    importMappings.find {
      im => im.javaScope == scope && im.javaName == name
    }

  def isImportedName(name: SimpleName): Boolean =
    neededImports.exists(p => p.javaName == name.asString)
}

class ClassContext(
  val fileContext: FileContext,
  val classOrInterface: Option[ClassOrInterfaceDeclaration|EnumDeclaration],
  val parentClassContext: Option[ClassContext] = Option.empty,
  val extractedStatements: mutable.Buffer[ast.Statement] = ListBuffer(),
) extends FileContext(fileContext.projectContext, fileContext.packageName, fileContext.extractedExport, fileContext.neededImports, fileContext.localIdentifiers) {
  def addExtractedStatements(sts: List[ast.Statement]): Unit =
    extractedStatements.appendAll(sts)

  override def isImportable(scope: Option[String], name: String): Boolean =
    super.isImportable(scope, name) && (scope match {
      case None => classOrInterface.exists(c => c.getName.asString != name)
      case scopeVal: Some[String] => classOrInterface.exists(c => c.getName.asString != scopeVal.get)
    }) && parentClassContext.forall(c => c.isImportable(scope, name))

  def isClassName(name: SimpleName): Boolean =
    classOrInterface.exists(c => c.getName == name) || parentClassContext.exists(c => c.isClassName(name))

  def isInterface: Boolean =
    classOrInterface.exists(c => {
      c match
        case c: ClassOrInterfaceDeclaration => c.isInterface
        case _ => false
    }) || parentClassContext.exists(c => c.isInterface)
}

class ParameterContext(
  val classContext: ClassContext,
  val parameters: mutable.Buffer[ast.Parameter]
) extends ClassContext(classContext.fileContext, classContext.classOrInterface, classContext.parentClassContext, classContext.extractedStatements) {

  override def isLocal(name: SimpleName): Boolean =
    super.isLocal(name)
    ||
    parameters.exists(p => p.name.escapedText == name.getIdentifier)

  def isNonStaticMember(name: SimpleName): Boolean =
    classOrInterface.exists {
        _.getMembers.asScala.exists {
          case mem: FieldDeclaration =>
            !mem.isStatic && mem.getVariables.asScala.exists(v => v.getName == name)
          case mem: MethodDeclaration =>
            !mem.isStatic && mem.getName == name
          case _ => false
        }
      }
      &&
      !parameters.exists(p => p.name.escapedText == name.getIdentifier)

  def isStaticMember(name: SimpleName): Boolean =
    classOrInterface.exists {
      p => p match {
        // we treat enum constants as static members
        case e: EnumDeclaration => e.getEntries.asScala.exists {
          entry => entry.getName == name
        }
        case c: ClassOrInterfaceDeclaration => c.getMembers.asScala.exists {
          case mem: FieldDeclaration =>
            mem.isStatic && mem.getVariables.asScala.exists(v => v.getName == name)
          case mem: MethodDeclaration =>
            mem.isStatic && mem.getName == name
          case _ => false
        }
      }
    } && !parameters.exists(p => p.name.escapedText == name.getIdentifier)
}

def getTypeScope(`type`: ClassOrInterfaceType): Option[String] =
  `type`.getScope.toScala.map(t => t.getName.asString)

def getTypeName(`type`: ClassOrInterfaceType): String =
  `type`.getName.asString
