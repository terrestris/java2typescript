# Data Types
## Long
options: methodBody
```java
long serialVersionUID = 4902022702746614570L;
```
```typescript
let serialVersionUID: number = 4902022702746614570;
```

## List
options: methodBody
```java
List<T> a = new ArrayList();
List b = new ArrayList();
```
```typescript
let a: T[] = [];
let b: any[] = [];
```

## Collection
options: methodBody
```java
Collection<T> a = new Collection();
Collection b = new Collection();
```
```typescript
let a: T[] = [];
let b: any[] = [];
```

## Special number values
options: methodBody
```java
Double x = Double.NaN;
Double y = Double.POSITIVE_INFINITY;
Double z = Double.NEGATIVE_INFINITY;
Double.isNaN(12);
Double.isFinite(12);
Double.isInfinite(12);
Double.doubleToLongBits(12);
Double.longBitsToDouble(12);
Double a = Double.parseDouble("12");
Double.MAX_VALUE;
Double.compare(12, 13);
Double.valueOf("12");
```
```typescript
let x: number = NaN;
let y: number = Number.POSITIVE_INFINITY;
let z: number = Number.NEGATIVE_INFINITY;
Number.isNaN(12);
Number.isFinite(12);
!Number.isFinite(12);
Number(12);
Number(12);
let a: number = parseFloat("12");
Number.MAX_VALUE;
12 === 13;
parseFloat("12");
```
