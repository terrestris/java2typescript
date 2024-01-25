import {ts, printNode} from 'ts-morph'
const factory = ts.factory

const ast = factory.createSwitchStatement(
    factory.createIdentifier("num"),
    factory.createCaseBlock([])
)


console.log(JSON.stringify(ast, null, 2))

console.log(printNode(ast))
