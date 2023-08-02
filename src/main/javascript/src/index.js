import {createSourceFile, createPrinter, ScriptTarget, EmitHint} from "typescript";

process.stdin.resume();
process.stdin.setEncoding('utf-8');

let data = '';

process.stdin.on('data', function (chunk) {
    data += chunk;
});

process.stdin.on('end', function () {
    const sourceFile = createSourceFile("x.ts", '', ScriptTarget.Latest);
    const printer = createPrinter();
    var input = JSON.parse(data);
    const nodes = Array.isArray(input) ? input : [input];
    const result = nodes.map(n =>
        printer.printNode(EmitHint.Unspecified, n, sourceFile)
    ).join('');

    console.log(result);
});
