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

## throw function result
```java
class A {
    public Error method() {
        return new Error();
    }
    public void throwing() {
        throw method();
    }
}
```
```typescript
export class A {
    public method(): Error {
        return new Error();
    }
    public throwing(): void {
        throw this.method();
    }
}
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
