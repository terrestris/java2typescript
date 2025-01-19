# Interfaces
## Cloneable
```java
class A implements Cloneable {
    public Object clone() {
        try {
            Coordinate coord = (Coordinate) super.clone();

            return coord; // return the clone
        } catch (CloneNotSupportedException e) {
            Assert.shouldNeverReachHere(
                    "this shouldn't happen because this class is Cloneable");

            return null;
        }
    }
}
```
```typescript
export class A {
    public clone(): A {
        return structuredClone(this);
    }
}
```

## Comparable
```json
{
  "customImports": [
    {
      "javaName": "Comparable",
      "fixedPath": "customLocation/Comparable.ts"
    }
  ]
}
```
```java
class B implements Comparable<B> {
    public int compareTo(B o) {
        return 2;
    }
}
```
```typescript
import { Comparable } from "customLocation/Comparable.ts";
export class B implements Comparable<B> {
    public compareTo(o: B): number {
        return 2;
    }
}
```

## Serializable
```java
class C implements Serializable {
}
```
```typescript
export class C {
}
```