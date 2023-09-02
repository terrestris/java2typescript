java2typescript
===============

Software to convert a Java program to TypeScript. It is targeted for JTS.

Developed following TDD principles.

It uses the typescript compiler in a dedicated process to write typescript code.

Getting Started
---------------

Install sbt: https://docs.scala-lang.org/getting-started/sbt-track/getting-started-with-scala-and-sbt-on-the-command-line.html

Build typescript writer

```shell
cd /src/main/javascript
npm i
npm run build
```

Run Tests for Test-Driven-Development

```shell
sbt test
```

Update TypeScript Version
-------------------------

* Install new version of `ts-morph` in `/src/main/javascript`.
* Find enum `SyntaxKind` in `typescript.d.ts` (`node_modules/@ts-morph/common/lib/typescript.d.ts`)
* Convert enum to scala in `java2typescript/ast/SyntaxKind.scala`
