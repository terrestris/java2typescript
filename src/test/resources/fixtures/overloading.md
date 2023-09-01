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
}
```
```typescript
export class A {
    private distanceToConstraints(p: Point): number;
    private distanceToConstraints(x: number, y: number): number;
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
        throw new Error("overload does not exist");
    }
}
```
## Constructor overloading
```java
class A {
    public void A(Integer a, String b) {
    }
    public void A(Boolean c) {
    }
}
```
```typescript
export class A {
    public constructor(a: number, b: string);
    public constructor(c: boolean);
    public constructor(...args: any[]) {
        if (args.length === 2 && typeof args[0] === "number" && typeof args[1] === "string") {
            let a: number = args[0];
            let b: string = args[1];
        }
        if (args.length === 1 && typeof args[0] === "boolean") {
            let c: boolean = args[0];
        }
        throw new Error("overload does not exist");
    }
}
```
