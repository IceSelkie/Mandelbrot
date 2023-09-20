package val.primativedouble;

public class Value
{
  double data;

  public static final Value ZERO = val(0);
  public static final Value ONE = val(1);
  public static final Value TWO = val(2);
  public static final Value FOUR = val(4);

  // create
  public Value(double val) { this.data = val; }
  public Value(String val) { this.data = new Double(val); }
  public static Value val(double v) { return new Value(v); }
  public static Value val(String v) { return new Value(v); }

  // add
  public Value add(Value o) { return val(data+o.data); }
  public static Value add(Value a, Value b) { return val(a.data+b.data); }

  // multiply
  public Value mul(Value o) { return val(data*o.data); }
  public static Value mul(Value a, Value b) { return val(a.data*b.data); }

  // subtract
  public Value subtract(Value o) { return val(data-o.data); }
  public Value sub(Value o) { return val(data-o.data); }
  public static Value subtract(Value a, Value b) { return val(a.data-b.data); }
  public static Value sub(Value a, Value b) { return val(a.data-b.data); }

  // square
  public Value sq() { return val(data*data); }
  public static Value sq(Value a) { return val(a.data*a.data); }


  // comparison
  public boolean lessThan(Value o) { return data<o.data; }
  public static boolean lessThan(Value a,Value b) { return a.data<b.data; }

  // negate
  public Value neg() { return val(-data); }
  public static Value neg(Value a) { return val(-a.data); }

  // reciprocal
  public Value recip() { return val(1/data); }

  // toString
  @Override public String toString() { return ((Double)data).toString(); }
}
