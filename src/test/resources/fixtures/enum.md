# Enums
## Simple enum
```java
enum Test {
    A,
    B,
    C
}
```
```typescript
export enum Test {
    A,
    B,
    C
}
```

## Enum with static fields and methods
```json
{
  "customImports": [
    {
      "javaName": "EnumSet",
      "fixedPath": "customLocation/EnumSet.ts"
    }
  ]
}
```
```java
import java.util.EnumSet;

enum Test {
    X,
    Y;
    private static final EnumSet<Ordinate> XY = EnumSet.of(X, Y);
    public static EnumSet<Ordinate> createXY() { return XY.clone(); }
}
```
```typescript
import { EnumSet } from "customLocation/EnumSet.ts";
export enum Test {
    X,
    Y
}
let XY: EnumSet<Ordinate> = EnumSet.of(Test.X, Test.Y);
export function createXY(): EnumSet<Ordinate> {
    return XY.clone();
}
```

## Usage of enum
```java
enum Test {
    A,
    B,
    C;
    public static Test method() {
        return Test.A;
    }
}
```
```java
import Test;

class A {
    private boolean method() {
        Test a = Test.A;
        return a == Test.method();
    }
}
```
```typescript
export enum Test {
    A,
    B,
    C
}
export function method(): Test {
    return Test.A;
}
```
```typescript
import { Test, method } from "./Test.ts";
export class A {
    private method(): boolean {
        let a: Test = Test.A;
        return a === method();
    }
}
```
