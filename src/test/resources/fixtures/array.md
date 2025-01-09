# Arrays
## Array initializer
options: methodBody
```java
Coordinate[] coordinates = new Coordinate[] { centerPt.copy(), radiusPt.copy() };
Coordinate[] coords = { new Coordinate(), new Coordinate() };
```
```typescript
let coordinates: Coordinate[] = [this.centerPt.copy(), this.radiusPt.copy()];
let coords: Coordinate[] = [new Coordinate(), new Coordinate()];
```

## Array accessor
options: methodBody
```java
Integer num = arr[0];
```
```typescript
let num: number = this.arr[0];
```

## Array brackets position
options: methodBody
```java
Coordinate coordinate[] = new Coordinate[] {};
```
```typescript
let coordinate: Coordinate[] = [];
```
