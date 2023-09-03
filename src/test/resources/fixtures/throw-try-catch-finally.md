# throw, try, catch, finally
## throw
options: methodBody
```java
throw new Error("test");
```
```typescript
throw new Error("test");
```
## throw custom error
options: methodBody
```java
throw new IllegalArgumentException("test");
```
```typescript
throw new Error("IllegalArgumentException: " + "test");
```

## try catch finally
options: methodBody
```java
try {
    something();
}
catch (Error e) {
    somethingOnError();
}
finally {
    somethingFinally();
}
```
```typescript
try {
    something();
}
catch (e: any) {
    somethingOnError();
}
finally {
    somethingFinally();
}
```
