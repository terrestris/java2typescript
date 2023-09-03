# Declarations

## string variable
options: methodBody
```java
String s = "abc";
```
```typescript
let s: string = "abc";
```

## integer variable
options: methodBody
```java
Integer i = 123;
```
```typescript
let i: number = 123;
```

## custom type
options: methodBody
```java
CustomType c = new CustomType();
```
```typescript
let c: CustomType = new CustomType();
```

## custom type with arguments
options: methodBody
```java
CustomType<String> c = new CustomType<>(123, "abc");
```
```typescript
let c: CustomType<string> = new CustomType(123, "abc");
```

## increment, decrement
options: methodBody
```java
i++;
i--;
++i;
--i;
i += 1;
i -= 2;
```
```typescript
i++;
i--;
++i;
--i;
i += 1;
i -= 2;
```
