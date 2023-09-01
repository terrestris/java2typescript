# Classes

## Basic class
```java
class A {}
```
```typescript
export class A {
}
```

## Basic interface
```java
interface A {}
```
```typescript
export interface A {
}
```

## With property members
```java
class A {
    private String a;
    protected String b;
    public String c;
    String d;
}
```
```typescript
export class A {
    private a: string;
    protected b: string;
    public c: string;
    d: string;
}
```

## With constructor member
```java
class A {
    public void A(Double param) {
        this.param = param;
    }
}
```
```typescript
export class A {
    public constructor(param: number) {
        this.param = param;
    }
}
```

## With function member
```java
class A {
    private String method(Boolean param) {
        return param;
    }
}
```
```typescript
export class A {
    private method(param: boolean): string {
        return param;
    }
}
```
