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
