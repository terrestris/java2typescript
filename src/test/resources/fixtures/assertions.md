# Assertions
## Assertions with message
options: methodBody
```java
assert true == false : "true should be false";
```
```typescript
if (!(true === false))
    throw new Error("true should be false");
```

## Assertions without message
options: methodBody
```java
assert x == 132;
```
```typescript
if (!(x === 132))
    throw new Error("Assertion failed");
```
