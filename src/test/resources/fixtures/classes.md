# Classes

## Basic class
```java
class A {}
```
```typescript
export class A {
}
```

## Basic interface
```java
interface A {}
```
```typescript
export interface A {
}
```

## With property members
```java
class A {
    private String a;
    protected String b;
    public String c;
    String d;
}
```
```typescript
export class A {
    private a: string;
    protected b: string;
    public c: string;
    d: string;
}
```

## With constructor member
```java
class A {
    public A(Double param) {
        this.param = param;
    }
}
```
```typescript
export class A {
    public constructor(param: number) {
        this.param = param;
    }
}
```

## With function member
```java
class A {
    private String method(Boolean param) {
        return param;
    }
}
```
```typescript
export class A {
    private method(param: boolean): string {
        return param;
    }
}
```

## Identify member access
```java
class A {
    private Integer memVar = 0;
    public Integer someMethod() {
        return internalMethod() + 2;
    }
    private Integer internalMethod() {
        return memVar - 2;
    }
}
```
```typescript
export class A {
    private memVar: number = 0;
    public someMethod(): number {
        return this.internalMethod() + 2;
    }
    private internalMethod(): number {
        return this.memVar - 2;
    }
}
```

## Extract internal classes
```java
class A {
    private class B {
    }
}
```
```typescript
export class A {
}
class B {
}
```

## static methods
```java
class A {
    public static Point getCenter(Geometry obstacles, double tolerance) {
        LargestEmptyCircle lec = new LargestEmptyCircle(obstacles, tolerance);
        return lec.getCenter();
    }
}
```
```typescript
export class A {
    public static getCenter(obstacles: Geometry, tolerance: number): Point {
        let lec: LargestEmptyCircle = new LargestEmptyCircle(obstacles, tolerance);
        return lec.getCenter();
    }
}
```

## parameters that have the same name as members
```java
class A {
    Integer name;
    Integer func(Integer name) {
        return name;
    }
    Integer func2() {
        return name;
    }
}
```
```typescript
export class A {
    name: number;
    func(name: number): number {
        return name;
    }
    func2(): number {
        return this.name;
    }
}
```
