# Control structures

## if
options: methodBody
```java
if (3 == 5) {
    return "equal";
}
```
```typescript
if (3 === 5) {
    return "equal";
}
```

## if else
options: methodBody
```java
if (3 <= 5) {
    return "less than or equal";
} else {
    return "greater";
}
```
```typescript
if (3 <= 5) {
    return "less than or equal";
}
else {
    return "greater";
}
```

## if else if else
options: methodBody
```java
if (3 > 5) {
    return "greater";
} else if (3 < 5) {
    return "smaller";
} else {
    return "equal";
}
```
```typescript
if (3 > 5) {
    return "greater";
}
else if (3 < 5) {
    return "smaller";
}
else {
    return "equal";
}
```

## while
options: methodBody
```java
while (num < 4) {
    num = func();
}
```
```typescript
while (num < 4) {
    num = func();
}
```

## for
options: methodBody
```java
for (int i = start; ; i += inc) {
    if (i >= max) {
        break;
    } else {
        continue;
    }
}
```
```typescript
for (let i: number = start;; i += inc) {
    if (i >= max) {
        break;
    }
    else {
        continue;
    }
}
```