# Overloading

## method overloading
```java
class A {
    private double distanceToConstraints(Point p) {
        return 1;
    }

    private double distanceToConstraints(double x, double y) {
        return 2;
    }

    private double distanceToConstraints(double[] coordinates) {
        return 3;
    }
}
```
```typescript
export class A {
    private distanceToConstraints(p: Point): number;
    private distanceToConstraints(x: number, y: number): number;
    private distanceToConstraints(coordinates: number[]): number;
    private distanceToConstraints(...args: any[]): number {
        if (args.length === 1 && args[0] instanceof Point) {
            let p: Point = args[0];
            return 1;
        }
        if (args.length === 2 && typeof args[0] === "number" && typeof args[1] === "number") {
            let x: number = args[0];
            let y: number = args[1];
            return 2;
        }
        if (args.length === 1 && Array.isArray(args[0])) {
            let coordinates: number[] = args[0];
            return 3;
        }
        throw new Error("overload does not exist");
    }
}
```

## Constructor overloading
```java
class A {
    public A(Integer a, String b) {
    }
    public A(Boolean c[]) {
    }
}
```
```typescript
export class A {
    public constructor(a: number, b: string);
    public constructor(c: boolean[]);
    public constructor(...args: any[]) {
        if (args.length === 2 && typeof args[0] === "number" && typeof args[1] === "string") {
            let a: number = args[0];
            let b: string = args[1];
        }
        if (args.length === 1 && Array.isArray(args[0])) {
            let c: boolean[] = args[0];
        }
        throw new Error("overload does not exist");
    }
}
```

## abstract overload
```java
abstract class CoordinateOperation implements GeometryEditorOperation {
    public final Geometry edit(Geometry geometry, GeometryFactory factory) {
        if (geometry instanceof LineString) {
            return factory.createLineString(edit(geometry.getCoordinates(),
                    geometry));
        }
        
        return geometry;
    }

    public abstract Coordinate[] edit(Coordinate[] coordinates, Geometry geometry);
}
```
```typescript
export abstract class CoordinateOperation implements GeometryEditorOperation {
    public edit(geometry: Geometry, factory: GeometryFactory): Geometry {
        if (geometry instanceof LineString) {
            return factory.createLineString(this.edit(geometry.getCoordinates(), geometry));
        }
        return geometry;
    }
}
```

## static methods with same name as non-static methods
```java
class A {
    public static boolean method(String b) {
        return false;
    }
    public boolean method(Integer c) {
        return true;
    }
    public boolean other() {
        return method("b") && method(5);
    }
}
```
```typescript
export class A {
    public static method(b: string): boolean {
        return false;
    }
    public method(c: number): boolean;
    public method(b: string): boolean;
    public method(...args: any[]): boolean {
        if (args.length === 1 && typeof args[0] === "number") {
            let c: number = args[0];
            return true;
        }
        if (args.length === 1 && typeof args[0] === "string") {
            let b: string = args[0];
            return A.method(b);
        }
        throw new Error("overload does not exist");
    }
    public other(): boolean {
        return this.method("b") && this.method(5);
    }
}
```
