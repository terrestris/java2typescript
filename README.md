# java2typescript

Software to convert a Java program to TypeScript. It is targeted for JTS.

Developed following TDD principles.

It uses the typescript compiler in a dedicated process to write typescript code.

## Getting Started

### Install sbt
https://docs.scala-lang.org/getting-started/sbt-track/getting-started-with-scala-and-sbt-on-the-command-line.html

### Build typescript writer

```shell
cd /src/main/javascript
npm i
npm run build
```

### Run Tests for Test-Driven-Development

```shell
sbt test
```

Most tests are defined by fixtures, they are `.md` files that can be found in [/src/test/resources/fixtures]().

They follow a special format:

````md
# Title of Suite
## Title of single test
options: optionA, optionB
```json
{
    the config to use for this test
}
```
```java
class A {
    the java class to convert
}
```
```typescript
class A {
    the typescript code that should be generated
}
```
````

if you want to debug a fixture, you can add the line `options: debug` right under the title of the fixture like this:
```md
# Overloading

## method overloading
options: debug
```

This will cause the `FixtureSpec` test to only run this single fixture and always fail (so it won't pass the ci). You can then debug the `FixtureSpec` test and set a breakpoint anywhere.

### Run program

```shell
sbt "run config/j2ts-config.json"
```

Run program for selected files (config is still required)

```shell
sbt "run config/j2ts-config.json ../jsts/jts/modules/core/src/main/java/org/locationtech/jts/geom/impl/PackedCoordinateSequence.java"
```

## Typescript AST Viewer

If you want to see how the typescript AST looks, you can use https://ts-ast-viewer.com

## Update TypeScript Version

* Install new version of `ts-morph` in `/src/main/javascript`.
* Find enum `SyntaxKind` in `typescript.d.ts` (`node_modules/@ts-morph/common/lib/typescript.d.ts`)
* Convert enum to scala in `java2typescript/ast/SyntaxKind.scala`

## How does it work?

The program runs in multiple steps:

1. It reads a config file that contains context information about the code that should be translated. An example lives in `config/j2ts-config.json`.
2. It gathers all configured files and reads them into memory. It uses regex replacements from the config file to replace some java constructs with other constructs.
3. It analyzes all exports. Later in the code types that are used are checked against this list via `context.addImportIfNeeded`.
4. It creates a project context that contains this information
5. It parses all files one-by-one and creates a typescript ast out of it
6. It takes the ast for a file and starts a javascript program using the typescript compiler to convert the file into typescript code.
7. It writes the generated typescript code into the target files

## TODOs

* CURRENT: Point.java / Geometry.java

* Improve documentation
* methods from inherited classes need to be prefixed by this
  * At the moment it works like this: for a given name the program checks if the name is a parameter of a method or a property of the class, if yes it prepends this.
    * this is not sufficient for names from parent class. if we would gather all local names (names in the class context, parameters and any local variables) we could determine this correctly.
* Find a clever way to transform constructs that work in Java into equivalent TypeScript structures
* super is called to late sometimes
* What to do with properties and methods with the same name??
* create drop in replacements for java builtins
  * List, ArrayList, Collection -> array
* Automatically filter files that contain un-translatable structures
  * Disabled at the moment 
  * Is done on class level at the moment -> move down to method level
  * replace by error throwing constructs
* What to do with System.getProperty? 
* Check if enum construct is sufficient
* Improve performance of the typescript code generation step
* Use const instead of let if possible

