# Expressions

## Addition
options: methodBody
```java
Double a = 2 + 3;
```
```typescript
let a: number = 2 + 3;
```

## Subtraction
options: methodBody
```java
Double a = 2 - 3;
```
```typescript
let a: number = 2 - 3;
```

## Multiplication
options: methodBody
```java
Double a = 2 * 3;
```
```typescript
let a: number = 2 * 3;
```

## Division
options: methodBody
```java
Double a = 2 / 3;
```
```typescript
let a: number = 2 / 3;
```

## Negative numbers
options: methodBody
```java
Double a = -3;
```
```typescript
let a: number = -3;
```

## Parenthesis
options: methodBody
```java
Double a = (3);
```
```typescript
let a: number = (3);
```

## Complex mathematical expression
options: methodBody
```java
Double a = -2 + 3 - 4 / 5 * (6 + 7);
```
```typescript
let a: number = -2 + 3 - 4 / 5 * (6 + 7);
```

## Complex boolean expression
options: methodBody
```java
Boolean x = true && false || true != true == false;
```
```typescript
let x: boolean = true && false || true !== true === false;
```

## cast expression
options: methodBody
```java
String text = (String) someVar;
```
```typescript
let text: string = someVar as string;
```

## conditional expression
options: methodBody
```java
Integer val = condition
    ? var1
    : var2;
```
```typescript
let val: number = condition ? var1 : var2;
```

## instanceof
options: methodBody
```java
Boolean x = a instanceof B;
```
```typescript
let x: boolean = a instanceof B;
```
