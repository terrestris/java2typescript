# Coordinate
## whole class
```json
{
  "customImports": [
    {
      "packageName": "org.locationtech.jts.util",
      "javaName": "Assert"
    },
    {
      "packageName": "org.locationtech.jts.util",
      "javaName": "NumberUtil"
    },
    {
      "javaName": "Comparable",
      "fixedPath": "customLocation/Comparable.ts"
    }
  ]
}
```
```java
/*
 * Copyright (c) 2018 Vivid Solutions
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.geom;

import java.io.Serializable;
import java.util.Comparator;

import org.locationtech.jts.util.Assert;
import org.locationtech.jts.util.NumberUtil;


/**
 * A lightweight class used to store coordinates on the 2-dimensional Cartesian plane.
 * <p>
 * It is distinct from {@link Point}, which is a subclass of {@link Geometry}. 
 * Unlike objects of type {@link Point} (which contain additional
 * information such as an envelope, a precision model, and spatial reference
 * system information), a <code>Coordinate</code> only contains ordinate values
 * and accessor methods. </p>
 * <p>
 * <code>Coordinate</code>s are two-dimensional points, with an additional Z-ordinate. 
 * If an Z-ordinate value is not specified or not defined, 
 * constructed coordinates have a Z-ordinate of <code>NaN</code>
 * (which is also the value of <code>NULL_ORDINATE</code>).  
 * The standard comparison functions ignore the Z-ordinate.
 * Apart from the basic accessor functions, JTS supports
 * only specific operations involving the Z-ordinate.</p> 
 * <p>
 * Implementations may optionally support Z-ordinate and M-measure values
 * as appropriate for a {@link CoordinateSequence}. 
 * Use of {@link #getZ()} and {@link #getM()}
 * accessors, or {@link #getOrdinate(int)} are recommended.</p> 
 *
 * @version 1.16
 */
public class Coordinate implements Comparable<Coordinate>, Cloneable, Serializable {
  private static final long serialVersionUID = 6683108902428366910L;
  
  /**
   * The value used to indicate a null or missing ordinate value.
   * In particular, used for the value of ordinates for dimensions 
   * greater than the defined dimension of a coordinate.
   */
  public static final double NULL_ORDINATE = Double.NaN;
  
  /** Standard ordinate index value for, where X is 0 */
  public static final int X = 0;

  /** Standard ordinate index value for, where Y is 1 */
  public static final int Y = 1;
  
  /**
   * Standard ordinate index value for, where Z is 2.
   *
   * <p>This constant assumes XYZM coordinate sequence definition, please check this assumption
   * using {@link CoordinateSequence#getDimension()} and {@link CoordinateSequence#getMeasures()}
   * before use.
   */
  public static final int Z = 2;

  /**
   * Standard ordinate index value for, where M is 3.
   *
   * <p>This constant assumes XYZM coordinate sequence definition, please check this assumption
   * using {@link CoordinateSequence#getDimension()} and {@link CoordinateSequence#getMeasures()}
   * before use.
   */
  public static final int M = 3;
  
  /**
   * The x-ordinate.
   */
  public double x;
  
  /**
   * The y-ordinate.
   */
  public double y;
  
  /**
   * The z-ordinate.
   * <p>
   * Direct access to this field is discouraged; use {@link #getZ()}.
   */
  public double z;

  /**
   *  Constructs a <code>Coordinate</code> at (x,y,z).
   *
   *@param  x  the x-ordinate
   *@param  y  the y-ordinate
   *@param  z  the z-ordinate
   */
  public Coordinate(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   *  Constructs a <code>Coordinate</code> at (0,0,NaN).
   */
  public Coordinate() {
    this(0.0, 0.0);
  }

  /**
   *  Constructs a <code>Coordinate</code> having the same (x,y,z) values as
   *  <code>other</code>.
   *
   *@param  c  the <code>Coordinate</code> to copy.
   */
  public Coordinate(Coordinate c) {
    this(c.x, c.y, c.getZ());
  }

  /**
   *  Constructs a <code>Coordinate</code> at (x,y,NaN).
   *
   *@param  x  the x-value
   *@param  y  the y-value
   */
  public Coordinate(double x, double y) {
    this(x, y, NULL_ORDINATE);
  }

  /**
   *  Sets this <code>Coordinate</code>s (x,y,z) values to that of <code>other</code>.
   *
   *@param  other  the <code>Coordinate</code> to copy
   */
  public void setCoordinate(Coordinate other) {
    x = other.x;
    y = other.y;
    z = other.getZ();
  }

  /**
   *  Retrieves the value of the X ordinate.
   *  
   *  @return the value of the X ordinate
   */  
  public double getX() {
    return x;
  }

  /**
   * Sets the X ordinate value.
   * 
   * @param x the value to set as X
   */
  public void setX(double x) {
    this.x = x;
  }
  
  /**
   *  Retrieves the value of the Y ordinate.
   *  
   *  @return the value of the Y ordinate
   */  
  public double getY() {
      return y;      
  }

  /**
   * Sets the Y ordinate value.
   * 
   * @param y the value to set as Y
   */
  public void setY(double y) {
    this.y = y;
  }
  
  /**
   *  Retrieves the value of the Z ordinate, if present.
   *  If no Z value is present returns <tt>NaN</tt>.
   *  
   *  @return the value of the Z ordinate, or <tt>NaN</tt>
   */   
  public double getZ() {
      return z;      
  }
  
  /**
   * Sets the Z ordinate value.
   * 
   * @param z the value to set as Z
   */
  public void setZ(double z) {
    this.z = z;
  }
  
  /**
   *  Retrieves the value of the measure, if present.
   *  If no measure value is present returns <tt>NaN</tt>.
   *  
   *  @return the value of the measure, or <tt>NaN</tt>
   */    
  public double getM() {
    return Double.NaN;     
  }
  
  /**
   * Sets the measure value, if supported.
   * 
   * @param m the value to set as M
   */
  public void setM(double m) {
    throw new IllegalArgumentException("Invalid ordinate index: " + M);
  }
  
  /**
   * Gets the ordinate value for the given index.
   * 
   * The base implementation supports values for the index are 
   * {@link #X}, {@link #Y}, and {@link #Z}.
   * 
   * @param ordinateIndex the ordinate index
   * @return the value of the ordinate
   * @throws IllegalArgumentException if the index is not valid
   */
  public double getOrdinate(int ordinateIndex)
  {
    switch (ordinateIndex) {
    case X: return x;
    case Y: return y;
    case Z: return getZ(); // sure to delegate to subclass rather than offer direct field access
    }
    throw new IllegalArgumentException("Invalid ordinate index: " + ordinateIndex);
  }
  
  /**
   * Sets the ordinate for the given index
   * to a given value.
   * 
   * The base implementation supported values for the index are 
   * {@link #X}, {@link #Y}, and {@link #Z}.
   * 
   * @param ordinateIndex the ordinate index
   * @param value the value to set
   * @throws IllegalArgumentException if the index is not valid
   */
  public void setOrdinate(int ordinateIndex, double value)
  {
    switch (ordinateIndex) {
      case X:
        x = value;
        break;
      case Y:
        y = value;
        break;
      case Z:
        setZ(value); // delegate to subclass rather than offer direct field access
        break;
      default:
        throw new IllegalArgumentException("Invalid ordinate index: " + ordinateIndex);
    }
  }

  /**
   * Tests if the coordinate has valid X and Y ordinate values.
   * An ordinate value is valid iff it is finite.
   * 
   * @return true if the coordinate is valid
   * @see Double#isFinite(double)
   */
  public boolean isValid() {
    if (! Double.isFinite(x)) return false;
    if (! Double.isFinite(y)) return false;
    return true;
  }
  
  /**
   *  Returns whether the planar projections of the two <code>Coordinate</code>s
   *  are equal.
   *
   *@param  other  a <code>Coordinate</code> with which to do the 2D comparison.
   *@return        <code>true</code> if the x- and y-coordinates are equal; the
   *      z-coordinates do not have to be equal.
   */
  public boolean equals2D(Coordinate other) {
    if (x != other.x) {
      return false;
    }
    if (y != other.y) {
      return false;
    }
    return true;
  }

  /**
   * Tests if another Coordinate has the same values for the X and Y ordinates,
   * within a specified tolerance value.
   * The Z ordinate is ignored.
   *
   *@param c a <code>Coordinate</code> with which to do the 2D comparison.
   *@param tolerance the tolerance value to use
   *@return true if <code>other</code> is a <code>Coordinate</code>
   *      with the same values for X and Y.
   */
  public boolean equals2D(Coordinate c, double tolerance){
    if (! NumberUtil.equalsWithTolerance(this.x, c.x, tolerance)) {
      return false;
    }
    if (! NumberUtil.equalsWithTolerance(this.y, c.y, tolerance)) {
      return false;
    }
    return true;
  }
  
  /**
   * Tests if another coordinate has the same values for the X, Y and Z ordinates.
   *
   *@param other a <code>Coordinate</code> with which to do the 3D comparison.
   *@return true if <code>other</code> is a <code>Coordinate</code>
   *      with the same values for X, Y and Z.
   */
  public boolean equals3D(Coordinate other) {
    return (x == other.x) && (y == other.y) &&
               ((getZ() == other.getZ()) ||
               (Double.isNaN(getZ()) && Double.isNaN(other.getZ())));
  }
  
  /**
   * Tests if another coordinate has the same value for Z, within a tolerance.
   * 
   * @param c a coordinate
   * @param tolerance the tolerance value
   * @return true if the Z ordinates are within the given tolerance
   */
  public boolean equalInZ(Coordinate c, double tolerance){
    return NumberUtil.equalsWithTolerance(this.getZ(), c.getZ(), tolerance);
  }
  
  /**
   *  Returns <code>true</code> if <code>other</code> has the same values for
   *  the x and y ordinates.
   *  Since Coordinates are 2.5D, this routine ignores the z value when making the comparison.
   *
   *@param  other  a <code>Coordinate</code> with which to do the comparison.
   *@return        <code>true</code> if <code>other</code> is a <code>Coordinate</code>
   *      with the same values for the x and y ordinates.
   */
  public boolean equals(Object other) {
    if (!(other instanceof Coordinate)) {
      return false;
    }
    return equals2D((Coordinate) other);
  }

  /**
   *  Compares this {@link Coordinate} with the specified {@link Coordinate} for order.
   *  This method ignores the z value when making the comparison.
   *  Returns:
   *  <UL>
   *    <LI> -1 : this.x &lt; other.x || ((this.x == other.x) &amp;&amp; (this.y &lt; other.y))
   *    <LI> 0 : this.x == other.x &amp;&amp; this.y = other.y
   *    <LI> 1 : this.x &gt; other.x || ((this.x == other.x) &amp;&amp; (this.y &gt; other.y))
   *
   *  </UL>
   *  Note: This method assumes that ordinate values
   * are valid numbers.  NaN values are not handled correctly.
   *
   *@param  o  the <code>Coordinate</code> with which this <code>Coordinate</code>
   *      is being compared
   *@return    -1, zero, or 1 as this <code>Coordinate</code>
   *      is less than, equal to, or greater than the specified <code>Coordinate</code>
   */
  public int compareTo(Coordinate o) {
    Coordinate other = (Coordinate) o;

    if (x < other.x) return -1;
    if (x > other.x) return 1;
    if (y < other.y) return -1;
    if (y > other.y) return 1;
    return 0;
  }

  /**
   *  Returns a <code>String</code> of the form <I>(x,y,z)</I> .
   *
   *@return    a <code>String</code> of the form <I>(x,y,z)</I>
   */
  public String toString() {
    return "(" + x + ", " + y + ", " + getZ() + ")";
  }

  public Object clone() {
    try {
      Coordinate coord = (Coordinate) super.clone();

      return coord; // return the clone
    } catch (CloneNotSupportedException e) {
      Assert.shouldNeverReachHere(
          "this shouldn't happen because this class is Cloneable");

      return null;
    }
  }
  
  /**
   * Creates a copy of this Coordinate.
   * 
   * @return a copy of this coordinate.
   */
  public Coordinate copy() {
    return new Coordinate(this);
  }
  
  /**
   * Create a new Coordinate of the same type as this Coordinate, but with no values.
   * 
   * @return a new Coordinate
   */
  public Coordinate create() {
      return new Coordinate();
  }

  /**
   * Computes the 2-dimensional Euclidean distance to another location.
   * The Z-ordinate is ignored.
   * 
   * @param c a point
   * @return the 2-dimensional Euclidean distance between the locations
   */
  public double distance(Coordinate c) {
    double dx = x - c.x;
    double dy = y - c.y;
    return Math.sqrt(dx * dx + dy * dy);
  }

  /**
   * Computes the 3-dimensional Euclidean distance to another location.
   * 
   * @param c a coordinate
   * @return the 3-dimensional Euclidean distance between the locations
   */
  public double distance3D(Coordinate c) {
    double dx = x - c.x;
    double dy = y - c.y;
    double dz = getZ() - c.getZ();
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  /**
   * Gets a hashcode for this coordinate.
   * 
   * @return a hashcode for this coordinate
   */
  public int hashCode() {
    //Algorithm from Effective Java by Joshua Bloch [Jon Aquino]
    int result = 17;
    result = 37 * result + hashCode(x);
    result = 37 * result + hashCode(y);
    return result;
  }

  /**
   * Computes a hash code for a double value, using the algorithm from
   * Joshua Bloch's book <i>Effective Java"</i>
   * 
   * @param x the value to compute for
   * @return a hashcode for x
   */
  public static int hashCode(double x) {
    long f = Double.doubleToLongBits(x);
    return (int)(f^(f>>>32));
  }


  /**
   * Compares two {@link Coordinate}s, allowing for either a 2-dimensional
   * or 3-dimensional comparison, and handling NaN values correctly.
   */
  public static class DimensionalComparator
      implements Comparator<Coordinate>
  {
    /**
     * Compare two <code>double</code>s, allowing for NaN values.
     * NaN is treated as being less than any valid number.
     *
     * @param a a <code>double</code>
     * @param b a <code>double</code>
     * @return -1, 0, or 1 depending on whether a is less than, equal to or greater than b
     */
    public static int compare(double a, double b)
    {
      if (a < b) return -1;
      if (a > b) return 1;

      if (Double.isNaN(a)) {
        if (Double.isNaN(b)) return 0;
        return -1;
      }

      if (Double.isNaN(b)) return 1;
      return 0;
    }

    private int dimensionsToTest = 2;

    /**
     * Creates a comparator for 2 dimensional coordinates.
     */
    public DimensionalComparator()
    {
      this(2);
    }

    /**
     * Creates a comparator for 2 or 3 dimensional coordinates, depending
     * on the value provided.
     *
     * @param dimensionsToTest the number of dimensions to test
     */
    public DimensionalComparator(int dimensionsToTest)
    {
      if (dimensionsToTest != 2 && dimensionsToTest != 3)
        throw new IllegalArgumentException("only 2 or 3 dimensions may be specified");
      this.dimensionsToTest = dimensionsToTest;
    }

    /**
     * Compares two {@link Coordinate}s along to the number of
     * dimensions specified.
     *
     * @param c1 a {@link Coordinate}
     * @param c2 a {link Coordinate}
     * @return -1, 0, or 1 depending on whether o1 is less than,
     * equal to, or greater than 02
     *
     */
    public int compare(Coordinate c1, Coordinate c2)
    {
      int compX = compare(c1.x, c2.x);
      if (compX != 0) return compX;

      int compY = compare(c1.y, c2.y);
      if (compY != 0) return compY;

      if (dimensionsToTest <= 2) return 0;

      int compZ = compare(c1.getZ(), c2.getZ());
      return compZ;
    }
  }

}
```
```typescript
import { NumberUtil } from "../util/NumberUtil.ts";
import { Comparable } from "customLocation/Comparable.ts";
export class Coordinate implements Comparable<Coordinate> {
    private static serialVersionUID: number = 6683108902428366910;
    public static NULL_ORDINATE: number = NaN;
    public static X: number = 0;
    public static Y: number = 1;
    public static Z: number = 2;
    public static M: number = 3;
    public x: number;
    public y: number;
    public z: number;
    public constructor(x: number, y: number, z: number);
    public constructor();
    public constructor(c: Coordinate);
    public constructor(x: number, y: number);
    public constructor(...args: any[]) {
        if (args.length === 3 && typeof args[0] === "number" && typeof args[1] === "number" && typeof args[2] === "number") {
            let x: number = args[0];
            let y: number = args[1];
            let z: number = args[2];
            this.x = x;
            this.y = y;
            this.z = z;
        }
        if (args.length === 0) {
            return new Coordinate(0.0, 0.0);
        }
        if (args.length === 1 && args[0] instanceof Coordinate) {
            let c: Coordinate = args[0];
            return new Coordinate(c.x, c.y, c.getZ());
        }
        if (args.length === 2 && typeof args[0] === "number" && typeof args[1] === "number") {
            let x: number = args[0];
            let y: number = args[1];
            return new Coordinate(x, y, Coordinate.NULL_ORDINATE);
        }
        throw new Error("overload does not exist");
    }
    public setCoordinate(other: Coordinate): void {
        this.x = other.x;
        this.y = other.y;
        this.z = other.getZ();
    }
    public getX(): number {
        return this.x;
    }
    public setX(x: number): void {
        this.x = x;
    }
    public getY(): number {
        return this.y;
    }
    public setY(y: number): void {
        this.y = y;
    }
    public getZ(): number {
        return this.z;
    }
    public setZ(z: number): void {
        this.z = z;
    }
    public getM(): number {
        return NaN;
    }
    public setM(m: number): void {
        throw new Error("IllegalArgumentException: " + "Invalid ordinate index: " + Coordinate.M);
    }
    public getOrdinate(ordinateIndex: number): number {
        switch (ordinateIndex) {
            case Coordinate.X: return this.x;
            case Coordinate.Y: return this.y;
            case Coordinate.Z: return this.getZ();
        }
        throw new Error("IllegalArgumentException: " + "Invalid ordinate index: " + ordinateIndex);
    }
    public setOrdinate(ordinateIndex: number, value: number): void {
        switch (ordinateIndex) {
            case Coordinate.X:
                this.x = value;
                break;
            case Coordinate.Y:
                this.y = value;
                break;
            case Coordinate.Z:
                this.setZ(value);
                break;
            default: throw new Error("IllegalArgumentException: " + "Invalid ordinate index: " + ordinateIndex);
        }
    }
    public isValid(): boolean {
        if (!Number.isFinite(this.x))
            return false;
        if (!Number.isFinite(this.y))
            return false;
        return true;
    }
    public equals2D(other: Coordinate): boolean;
    public equals2D(c: Coordinate, tolerance: number): boolean;
    public equals2D(...args: any[]): boolean {
        if (args.length === 1 && args[0] instanceof Coordinate) {
            let other: Coordinate = args[0];
            if (this.x !== other.x) {
                return false;
            }
            if (this.y !== other.y) {
                return false;
            }
            return true;
        }
        if (args.length === 2 && args[0] instanceof Coordinate && typeof args[1] === "number") {
            let c: Coordinate = args[0];
            let tolerance: number = args[1];
            if (!NumberUtil.equalsWithTolerance(this.x, c.x, tolerance)) {
                return false;
            }
            if (!NumberUtil.equalsWithTolerance(this.y, c.y, tolerance)) {
                return false;
            }
            return true;
        }
        throw new Error("overload does not exist");
    }
    public equals3D(other: Coordinate): boolean {
        return (this.x === other.x) && (this.y === other.y) && ((this.getZ() === other.getZ()) || (Number.isNaN(this.getZ()) && Number.isNaN(other.getZ())));
    }
    public equalInZ(c: Coordinate, tolerance: number): boolean {
        return NumberUtil.equalsWithTolerance(this.getZ(), c.getZ(), tolerance);
    }
    public equals(other: Object): boolean {
        if (!(other instanceof Coordinate)) {
            return false;
        }
        return this.equals2D(other as Coordinate);
    }
    public compareTo(o: Coordinate): number {
        let other: Coordinate = o as Coordinate;
        if (this.x < other.x)
            return -1;
        if (this.x > other.x)
            return 1;
        if (this.y < other.y)
            return -1;
        if (this.y > other.y)
            return 1;
        return 0;
    }
    public toString(): string {
        return "(" + this.x + ", " + this.y + ", " + this.getZ() + ")";
    }
    public clone(): Coordinate {
        return structuredClone(this);
    }
    public copy(): Coordinate {
        return new Coordinate(this);
    }
    public create(): Coordinate {
        return new Coordinate();
    }
    public distance(c: Coordinate): number {
        let dx: number = this.x - c.x;
        let dy: number = this.y - c.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    public distance3D(c: Coordinate): number {
        let dx: number = this.x - c.x;
        let dy: number = this.y - c.y;
        let dz: number = this.getZ() - c.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    public static hashCode(x: number): number {
        let f: number = Number(x);
        return Math.floor((f ^ (f >>>= 32)));
    }
    public hashCode(): number;
    public hashCode(x: number): number;
    public hashCode(...args: any[]): number {
        if (args.length === 0) {
            let result: number = 17;
            result = 37 * result + this.hashCode(this.x);
            result = 37 * result + this.hashCode(this.y);
            return result;
        }
        if (args.length === 1 && typeof args[0] === "number") {
            let x: number = args[0];
            return Coordinate.hashCode(x);
        }
        throw new Error("overload does not exist");
    }
}
export class DimensionalComparator implements Comparator<Coordinate> {
    private dimensionsToTest: number = 2;
    public constructor();
    public constructor(dimensionsToTest: number);
    public constructor(...args: any[]) {
        if (args.length === 0) {
            return new DimensionalComparator(2);
        }
        if (args.length === 1 && typeof args[0] === "number") {
            let dimensionsToTest: number = args[0];
            if (dimensionsToTest !== 2 && dimensionsToTest !== 3)
                throw new Error("IllegalArgumentException: " + "only 2 or 3 dimensions may be specified");
            this.dimensionsToTest = dimensionsToTest;
        }
        throw new Error("overload does not exist");
    }
    public static compare(a: number, b: number): number {
        if (a < b)
            return -1;
        if (a > b)
            return 1;
        if (Number.isNaN(a)) {
            if (Number.isNaN(b))
                return 0;
            return -1;
        }
        if (Number.isNaN(b))
            return 1;
        return 0;
    }
    public compare(c1: Coordinate, c2: Coordinate): number;
    public compare(a: number, b: number): number;
    public compare(...args: any[]): number {
        if (args.length === 2 && args[0] instanceof Coordinate && args[1] instanceof Coordinate) {
            let c1: Coordinate = args[0];
            let c2: Coordinate = args[1];
            let compX: number = this.compare(c1.x, c2.x);
            if (compX !== 0)
                return compX;
            let compY: number = this.compare(c1.y, c2.y);
            if (compY !== 0)
                return compY;
            if (this.dimensionsToTest <= 2)
                return 0;
            let compZ: number = this.compare(c1.getZ(), c2.getZ());
            return compZ;
        }
        if (args.length === 2 && typeof args[0] === "number" && typeof args[1] === "number") {
            let a: number = args[0];
            let b: number = args[1];
            return DimensionalComparator.compare(a, b);
        }
        throw new Error("overload does not exist");
    }
}
```