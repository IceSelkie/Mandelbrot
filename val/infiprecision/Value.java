package val.infiprecision;

import java.math.BigDecimal;
import java.math.MathContext;

public class Value
{
  private BigDecimal data;
  public static final Value ZERO = val(BigDecimal.ZERO);
  public static final Value ONE = val(BigDecimal.ONE);
  public static final Value TWO = val(new BigDecimal(2));
  public static final Value FOUR = val(new BigDecimal(4));

  // create
  public Value(BigDecimal val) { this.data = val; }
  public Value(String val) { this.data = new BigDecimal(val); }
  public static Value val(BigDecimal v) { return new Value(v); }
  public static Value val(String v) { return new Value(v); }

  // add
  public Value add(Value o) { return val(data.add(o.data)); }
  public static Value add(Value a, Value b) { return val(a.data.add(b.data)); }

  // multiply
  public Value mul(Value o) { return val(data.multiply(o.data)); }
  public static Value mul(Value a, Value b) { return val(a.data.multiply(b.data)); }

  // subtract
  public Value sub(Value o) { return val(data.subtract(o.data)); }
  public static Value sub(Value a, Value b) { return val(a.data.subtract(b.data)); }

  // square
  public Value sq() { return mul(this); }
  public static Value sq(Value a) { return mul(a,a); }


  // comparison
  public boolean lessThan(Value o) { return data.compareTo(o.data)<0; }
  public static boolean lessThan(Value a,Value b) { return a.data.compareTo(b.data)<0; }

  // negate
  public Value neg() { return val(data.negate()); }
  public static Value neg(Value a) { return val(a.data.negate()); }

  // reciprocal
  public Value recip() { return val(BigDecimal.ONE.divide(data, new MathContext(data.precision()))); }

  // toString
  @Override public String toString() { return data.toString(); }
}
