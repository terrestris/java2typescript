# Unsupported Keywords
## Class with unsupported keyword
```java
class Some {
    protected transient SoftReference<Coordinate[]> coordRef;

    protected PackedCoordinateSequence(int dimension, int measures) {
        if (dimension - measures < 2) {
            throw new IllegalArgumentException("Must have at least 2 spatial dimensions");
        }
        this.dimension = dimension;
        this.measures = measures;
    }

    public int getDimension() {
        return this.dimension;
    }

    public static String toString() {
        return CoordinateSequences.toString(this);
    }
}
```

```typescript
export class Some {
    protected constructor(dimension: number, measures: number) {
        throw new Error("This class uses features that are not supported by javascript");
    }
    public static toString(): string {
        throw new Error("This class uses features that are not supported by javascript");
    }
    public toString(): string {
        return Some.toString();
    }
}
```
