package val.quotientinfiprecision;

import java.math.BigInteger;

public class Value
{
  BigInteger n, d;

  public static final Value ZERO = val(0,1);
  public static final Value ONE = val(1,1);
  public static final Value TWO = val(2,1);
  public static final Value FOUR = val(4,1);

  // create
  public Value(BigInteger n, BigInteger d) { this.n = n;this.d = d; }
  public Value(long n, long d) { this.n = new BigInteger(n + "");this.d = new BigInteger(d + ""); }
  public static Value val(BigInteger n, BigInteger d) { return new Value(n,d); }
  public static Value val(long n, long d) { return new Value(n,d); }


  // add
  public Value add(Value o) { return val(n.multiply(o.d).add(o.n.multiply(d)), d.multiply(o.d)); }
  public static Value add(Value a, Value b) { return val(a.n.multiply(b.d).add(b.n.multiply(a.d)), a.d.multiply(b.d)); }

  // multiply
  public Value mul(Value o) { return val(n.multiply(o.n),d.multiply(o.d)); }
  public static Value mul(Value a, Value b) { return val(a.n.multiply(b.n),a.d.multiply(b.d)); }

  // subtract
  public Value sub(Value o) { return val(n.multiply(o.d).subtract(o.n.multiply(d)), d.multiply(o.d)); }
  public static Value sub(Value a, Value b) { return val(a.n.multiply(b.d).subtract(b.n.multiply(a.d)), a.d.multiply(b.d)); }

  // square
  public Value sq() { return mul(this); }
  public static Value sq(Value a) { return mul(a,a); }


  // comparison
  public boolean lessThan(Value o) { return n.multiply(o.d).compareTo(o.n.multiply(d))<0; }
  public static boolean lessThan(Value a,Value b) { return a.n.multiply(b.d).compareTo(b.n.multiply(a.d))<0; }

  // negate
  public Value neg() { return val(n.negate(),d); }
  public static Value neg(Value a) { return val(a.n.negate(),a.d); }

  // reciprocal
  public Value recip() { return val(d,n); }

  // toString
  @Override public String toString() {
    return new java.math.BigDecimal(n).multiply(new java.math.BigDecimal("1000000000")).divide(new java.math.BigDecimal(d), java.math.BigDecimal.ROUND_HALF_UP).divide(new java.math.BigDecimal("1000000000")).toString();
  }
}
