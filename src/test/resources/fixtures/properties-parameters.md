# Properties vs Parameters
## Using Parameters that have the same name as properties
```java
class A {
    private Geometry obstacles;
    private double tolerance;

    public static LineString getRadiusLine(Geometry obstacles, double tolerance) {
        LargestEmptyCircle lec = new LargestEmptyCircle(obstacles, tolerance);
        return lec.getRadiusLine();
    }
}
```
```typescript
export class A {
    private obstacles: Geometry;
    private tolerance: number;
    public static getRadiusLine(obstacles: Geometry, tolerance: number): LineString {
        let lec: LargestEmptyCircle = new LargestEmptyCircle(obstacles, tolerance);
        return lec.getRadiusLine();
    }
}
```

## Using local vs non local names
```java
class A extends X {
    private int a;
    protected int b() {
        return 2;
    }
    public static int c() {
        return 3;
    }
    public void method(int d) {
        int e = 4;
        nonLocal(a, b(), c(), d, e);
    }
}
```
```typescript
export class A extends X {
    private a: number;
    protected b(): number {
        return 2;
    }
    public static c(): number {
        return 3;
    }
    public method(d: number): void {
        let e: number = 4;
        this.nonLocal(this.a, this.b(), A.c(), d, e);
    }
}
```
