// import {createSourceFile, createPrinter, forEachChild, ScriptTarget, SyntaxKind, EmitHint, factory} from "typescript";

import {Project, printNode, ts} from "ts-morph"

process.stdin.resume();
process.stdin.setEncoding('utf-8');

let data = '';

process.stdin.on('data', function (chunk) {
    data += chunk;
});

process.stdin.on('end', function () {
    var input = JSON.parse(data);
    console.log(writeCode(input));
});

function transformNode(node) {
    node.transform(traversal => {
        const node = traversal.visitChildren();

        if (ts.isBlock(node)) {
            return traversal.factory.createBlock(node.statements, true);
        }
        return node;
    });
}

function writeCode(input) {
    const nodes = Array.isArray(input) ? input : [input];
    const code = nodes.map(printNode).join('\n');

    const project = new Project();
    const sourceFile = project.createSourceFile("x.ts", code);

    transformNode(sourceFile);

    return sourceFile.compilerNode.statements.map(printNode).join('\n');
}
