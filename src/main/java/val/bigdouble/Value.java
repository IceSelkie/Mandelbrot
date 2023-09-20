package val.bigdouble;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class Value
{
  BigDecimal data;
  // 7 -> 32; 16 -> 64; 34 -> 128
  // MathContext(16, RoundingMode.HALF_EVEN)
  public static final MathContext precision = MathContext.DECIMAL128;

  public static final Value ZERO = val(0);
  public static final Value ONE = val(1);
  public static final Value TWO = val(2);
  public static final Value FOUR = val(4);

  // create
  public static Value val(BigDecimal val) { return new Value(val); }
  public static Value val(double val) { return new Value(val); }
  public static Value val(String val) { return new Value(val); }

  public Value(BigDecimal val) { this.data = val; }
  public Value(double val) { this.data = new BigDecimal(val); }
  public Value(String val) { this.data = new BigDecimal(val); }


  // add
  public Value add(Value o) { return val(data.add(o.data,precision)); }
  public static Value add(Value a, Value b) { return val(a.data.add(b.data,precision)); }

  // multiply
  public Value mul(Value o) { return val(data.multiply(o.data,precision)); }
  public static Value mul(Value a, Value b) { return val(a.data.multiply(b.data,precision)); }

  // subtract
  public Value sub(Value o) { return val(data.subtract(o.data,precision)); }
  public static Value sub(Value a, Value b) { return val(a.data.subtract(b.data,precision)); }

  // square
  public Value sq() { return mul(this); }
  public static Value sq(Value a) { return mul(a,a); }


  // comparison
  public boolean lessThan(Value o) { return data.compareTo(o.data)<0; }
  public static boolean lessThan(Value a,Value b) { return a.data.compareTo(b.data)<0; }

  // negate
  public Value neg() { return val(data.negate(precision)); }
  public static Value neg(Value a) { return val(a.data.negate(precision)); }

  // reciprocal
  public Value recip() { return val(BigDecimal.ONE.divide(data,precision)); }

  // toString
  @Override public String toString() {
    return data.toString();
    //return new java.math.BigDecimal(n).multiply(new java.math.BigDecimal("1000000000")).divide(new java.math.BigDecimal(d), java.math.BigDecimal.ROUND_HALF_UP).divide(new java.math.BigDecimal("1000000000")).toString();
  }
}
