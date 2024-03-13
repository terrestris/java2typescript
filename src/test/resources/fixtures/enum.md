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
```java
enum Test {
    A,
    B;
    private static final EnumSet<Ordinate> XY = EnumSet.of(X, Y);
    public static EnumSet<Ordinate> createXY() { return XY.clone(); }
}
```
```typescript
export enum Test {
    A,
    B
}
let XY: EnumSet<Ordinate> = EnumSet.of(Test.X, Test.Y);
export function createXY(): EnumSet<Ordinate> { return XY.clone(); }
```

## Usage of enum
options: debug
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
import { Test, method } from "Test.ts";
class A {
    private method(): boolean {
      return a == method();
    }
}
```
