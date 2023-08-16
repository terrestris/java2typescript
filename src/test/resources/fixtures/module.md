# Module

## Empty
```java
```
```typescript
```

## Class
```java
class A {}
```
```typescript
export class A {
}
```

## Import
```java
package a.b.c;
import a.b.D;
```
```typescript
import { D } from "../D.ts";
```

## Import & class
```java
package a.b.c;
import a.b.D;
class A {}
```
```typescript
import { D } from "../D.ts";
export class A {
}
```
