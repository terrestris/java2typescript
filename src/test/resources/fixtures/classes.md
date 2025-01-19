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
interface A {
}
```
```typescript
export abstract class A {
}
```

## Interface with method
```java
interface A {
    void iMethod();
}
```
```typescript
export abstract class A {
    public abstract iMethod(): void;
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
export class B {
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
    public getCenter(obstacles: Geometry, tolerance: number): Point {
        return A.getCenter(obstacles, tolerance);
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

## static members
```java
class A {
    final static Integer VAR = 2;
    static Integer func() {
        return 2;
    }
    Integer func2() {
        return VAR + func();
    }
}
```
```typescript
export class A {
    static VAR: number = 2;
    static func(): number {
        return 2;
    }
    func(): number {
        return A.func();
    }
    func2(): number {
        return A.VAR + A.func();
    }
}
```

## inheritance
```java
class A extends B<C> {
    public A() {
        super();
    }
    public void method() {
        super.method();
    }
}
```
```typescript
export class A extends B<C> {
    public constructor() {
        super();
    }
    public method(): void {
        super.method();
    }
}
```

## implementing interface
```java
class A implements B, C<D> {
    
}
```
```typescript
export class A implements B, C<D> {
}
```

## static initializer
```java
import System;

class GeometryOverlay {
    static String OVERLAY_PROPERTY_NAME = "test";
    static {
        setOverlayImpl(OVERLAY_PROPERTY_NAME);
    }
    static void setOverlayImpl(String overlayImplCode) {
    }
}
```
```typescript
export class GeometryOverlay {
    static OVERLAY_PROPERTY_NAME: string = "test";
    static setOverlayImpl(overlayImplCode: string): void {
    }
    setOverlayImpl(overlayImplCode: string): void {
        return GeometryOverlay.setOverlayImpl(overlayImplCode);
    }
}
GeometryOverlay.setOverlayImpl(GeometryOverlay.OVERLAY_PROPERTY_NAME);
```

## abstract class
```java
abstract class A {
    abstract Integer func();
    Integer func2() {
        return func();
    }
}
```
```typescript
export abstract class A {
    abstract func(): number;
    func2(): number {
        return this.func();
    }
}
```

## multiple members
```java
class A {
    private double m00, m01;
}
```
```typescript
export class A {
    private m00: number;
    private m01: number;
}
```

## Nested Classes
```java
class A {
    public class B {
    }
}
```
```java
import A;

class C {
    private A.B ab = new A.B();
}
```
```typescript
export class A {
}
export class B {
}
```
```typescript
import { B } from "./A.ts";
export class C {
    private ab: B = new B();
}
```

## Nested Classes with typed interface using parent class
```java
interface I<T> {
    boolean method(T param);
}
```
```java
import I;

class A {
    public class B implements I<A> {
        public boolean method(A param) {
            return true;
        }
    }
}
```
```java
import A;

class C {
    private A.B ab = new A.B();
}
```
```typescript
export abstract class I<T> {
    public abstract method(param: T): boolean;
}
```
```typescript
import { I } from "./I.ts";
export class A {
}
export class B implements I<A> {
    public method(param: A): boolean {
        return true;
    }
}
```
```typescript
import { B } from "./A.ts";
export class C {
    private ab: B = new B();
}
```

## Super Constructor
```java
class A {
    private String val;
    public A(String x) {
        val = x;
    }
}
```
```java
import A;

class B extends A {
    public B(String y) {
        super(y);
    }
}
```
```typescript
export class A {
    private val: string;
    public constructor(x: string) {
        this.val = x;
    }
}
```

```typescript
import { A } from "./A.ts";
export class B extends A {
    public constructor(y: string) {
        super(y);
    }
}
```

## call constructor from constructor
```java
public class Coordinate {
    public static final double NULL_ORDINATE = Double.NaN;
    public double x;
    public double y;
    public double z;

    public Coordinate(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Coordinate(double x, double y) {
        this(x, y, NULL_ORDINATE);
    }

    public Coordinate() {
        this(0.0, 0.0);
    }
}
```
```typescript
export class Coordinate {
    public static NULL_ORDINATE: number = NaN;
    public x: number;
    public y: number;
    public z: number;
    public constructor(x: number, y: number, z: number);
    public constructor(x: number, y: number);
    public constructor();
    public constructor(...args: any[]) {
        if (args.length === 3 && typeof args[0] === "number" && typeof args[1] === "number" && typeof args[2] === "number") {
            let x: number = args[0];
            let y: number = args[1];
            let z: number = args[2];
            this.x = x;
            this.y = y;
            this.z = z;
        }
        if (args.length === 2 && typeof args[0] === "number" && typeof args[1] === "number") {
            let x: number = args[0];
            let y: number = args[1];
            return new Coordinate(x, y, Coordinate.NULL_ORDINATE);
        }
        if (args.length === 0) {
            return new Coordinate(0.0, 0.0);
        }
        throw new Error("overload does not exist");
    }
}
```
