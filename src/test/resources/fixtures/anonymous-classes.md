# Anonymous Classes
## simple anonymous class that implements interface
```java
interface Filter {
    boolean filter(String x);
}
```
```java
import Filter;
class FilterFactory {
    public Filter getFilter() {
        return new Filter() {
            public boolean filter(String x) {
                return x == "value";
            }
        };
    }
}
```
```typescript
export abstract class Filter {
    public abstract filter(x: string): boolean;
}
```
```typescript
import { Filter } from "./Filter.ts";
export class FilterFactory {
    public getFilter(): Filter {
        return new (class extends Filter {
            public filter(x: string): boolean {
                return x === "value";
            }
        })();
    }
}
```
