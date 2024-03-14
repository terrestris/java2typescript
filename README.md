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
```
```
````

if you want to debug a fixture, you can add the line `options: debug` right under the title of the fixture like this:
```md
# Overloading

## method overloading
options: debug
```

This will cause the `FixtureSpec` test to only run this single fixture and allways fail (so it won't pass the ci). You can then debug the `FixtureSpec` test and set a breakpoint anywhere.

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

## TODOs

* add a first parsing step that only determines exports and maps them
  * add imports for classes from the same package
  * add different imports for nested classes
  * add different imports for static methods on enums
* add package.json
* add tsconfig
* create drop in replacements for java builtins

parseExports

export -> import
class XY -> import {XY} from "filename"
in code
new XY or XY.staticMethod (same)

class XY { class AB }
->
class XY {}
class XY_AB {}
->
import {XY, AB} from "filename"

in code
new XY.AB
->
new XY_AB

enum XY { static something() }
->
enum XY {}
XY_something()


-> import {XY, something} from "filename"
in code
XY.something -> something
