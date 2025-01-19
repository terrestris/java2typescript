# Module

## Empty
```java
```
```typescript
```

## Class
```java
class A {}
```
```typescript
export class A {
}
```

## Used Import as Type
```java
package a.b;

class D {
}
```
```java
package a.b.c;
import a.b.D;

class A {
    private D member;
}
```
```typescript
export class D {
}
```
```typescript
import { D } from "../D.ts";
export class A {
    private member: D;
}
```

## Used Import as Constructor
```java
package a.b;

class D {
}
```
```java
package a.b.c;
import a.b.D;

class A {
    private void method() {
        var a = new D();
    }
}
```
```typescript
export class D {
}
```
```typescript
import { D } from "../D.ts";
export class A {
    private method(): void {
        let a = new D();
    }
}
```

## Used Import as Property Type
```java
package a.b;

class D {
}
```
```java
package a.b.c;
import a.b.D;

class A {
    private D d;
}
```
```typescript
export class D {
}
```
```typescript
import { D } from "../D.ts";
export class A {
    private d: D;
}
```

## Used Import with static call
```java
package a.b;

class D {
    static void staticFunc() {}
}
```
```java
package a.b.c;
import a.b.D;

class A {
    private void method() {
        D.staticFunc();
    }
}
```
```typescript
export class D {
    static staticFunc(): void {
    }
    staticFunc(): void {
        return D.staticFunc();
    }
}
```
```typescript
import { D } from "../D.ts";
export class A {
    private method(): void {
        D.staticFunc();
    }
}
```

## Used Import as static property access
```java
package a.b;

class D {
    public final static Integer x = 5;
}
```
```java
package a.b.c;
import a.b.D;

class A {
    private void method() {
        return D.x;
    }
}
```
```typescript
export class D {
    public static x: number = 5;
}
```
```typescript
import { D } from "../D.ts";
export class A {
    private method(): void {
        return D.x;
    }
}
```

## Unused Import
```java
package a.b.c;
import a.b.D;

class A {
}
```
```typescript
export class A {
}
```

## Import Config
```json
{
  "customImports": [
    {
      "javaName": "PriorityQueue",
      "fixedPath": "customLocation/PriorityQueue.ts"
    }
  ]
}
```
```java
import java.util.PriorityQueue;

class A {
    private PriorityQueue P;
}
```
```typescript
import { PriorityQueue } from "customLocation/PriorityQueue.ts";
export class A {
    private P: PriorityQueue;
}
```

## Do not import self
```java
class A {
    private static Integer a = 7;
    public Integer method() {
        return A.a;
    }
}
```
```typescript
export class A {
    private static a: number = 7;
    public method(): number {
        return A.a;
    }
}
```

## Import from same directory/package
```java
package a.b;

class D {
}
```
```java
package a.b;

class A {
    private D d;
}
```
```typescript
export class D {
}
```
```typescript
import { D } from "./D.ts";
export class A {
    private d: D;
}
```
