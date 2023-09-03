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

## Import Config
```json
{
  "imports": [
    {
      "class": "java.util.PriorityQueue",
      "location": "customLocation/PriorityQueue.ts"
    }
  ]
}
```
```java
import java.util.PriorityQueue;
```
```typescript
import { PriorityQueue } from "customLocation/PriorityQueue.ts";
```
