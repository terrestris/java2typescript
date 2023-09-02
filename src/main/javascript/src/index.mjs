import {printNode, ts} from "ts-morph"

process.stdin.resume();
process.stdin.setEncoding('utf-8');

ts.SyntaxKind

let data = '';

process.stdin.on('data', function (chunk) {
    data += chunk;
});

process.stdin.on('end', function () {
    var input = JSON.parse(data);
    console.log(writeCode(input));
});

function writeCode(input) {
    const nodes = Array.isArray(input) ? input : [input];
    return nodes.map(printNode).join('\n');
}
