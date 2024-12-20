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
while (this.num < 4) {
    this.num = this.func();
}
```

## for
options: methodBody
```java
for (int i = 1; ; i += 2) {
    if (i >= 10) {
        break;
    } else {
        continue;
    }
}
```
```typescript
for (let i: number = 1;; i += 2) {
    if (i >= 10) {
        break;
    }
    else {
        continue;
    }
}
```

## for2
options: methodBody
```java
int i = 2;
for (int j = 0; j < last; j++)
    newCoordinates[j] = coordinates[(i + j) % last];
```
```typescript
let i: number = 2;
for (let j: number = 0; j < this.last; j++)
    this.newCoordinates[j] = this.coordinates[(i + j) % this.last];
```

## switch
options: methodBody
```java
switch(num) {
    case 1:
        func();
        break;
    case 2:
    case 3:
        func2();
        break;
    default:
        func3();
}
```
```typescript
switch (this.num) {
    case 1:
        this.func();
        break;
    case 2:
    case 3:
        this.func2();
        break;
    default: this.func3();
}
```

## for each
options: methodBody
```java
for (Geometry hole : holesFixed) {
    holes.add(hole);
}
```
```typescript
for (let hole: Geometry of this.holesFixed) {
    this.holes.add(hole);
}
```

## do while
options: methodBody
```java
int i = 1;
do {
    i++;
} while (i < 10);
```
```typescript
let i: number = 1;
do {
    i++;
} while (i < 10);
```
