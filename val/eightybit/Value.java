package val.eightybit;

public class Value
{
  private static long MASK =        0x00000000FFFFFFFFL;
  private static long MASK_TOPBIT = 0x0000000080000000L;
  private static long MASK_LOWBIT = 0x0000000000000001L;
  boolean negative;
  int exponent;
  long mantissa_big; // lower half only used
  long mantissa_small; // lower half only used
  // Recombine with "(mantissa_big << 32) + mantissa_small"

//  public static final Value ZERO = val(0);
//  public static final Value ONE = val(1);
//  public static final Value TWO = val(2);
//  public static final Value FOUR = val(4);

  // create
//  public Value(double val) { this.data = val; }
//  public Value(String val) { this.data = new Double(val); }
//  public static Value val(double v) { return new Value(v); }
//  public static Value val(String v) { return new Value(v); }
  private void fix()
  {
    long evenHigher = mantissa_big>>32;
    mantissa_big = (mantissa_big&MASK)+(mantissa_small>>32);
    mantissa_small &= MASK;

    while (evenHigher>0)
    {
      mantissa_small = (mantissa_small>>1)+(mantissa_big&MASK_LOWBIT<<31);
      mantissa_big = (mantissa_big>>1)+(evenHigher&MASK_LOWBIT<<31);
      evenHigher >>= 1;
    }

    // If value is zero
    if (mantissa_big==0 && mantissa_small == 0)
    {
      negative = false;
      exponent = 0;
    }
    // Non-zero
    else
    {
      if ((mantissa_big & 0x8000000L)==0)
      {

      }
    }
  };

  // add
//  public Value add(Value o)
//  {
//    if (o.exponent>exponent)
//      return o.add(this);
//
//    // O is much smaller
//    if (o.exponent+64<exponent)
//      return this;
//
//    // O is smaller
//
//    fix();
//  }
//  public static Value add(Value a, Value b) { return val(a.data+b.data); }

//  // multiply
//  public Value mul(Value o) { return val(data*o.data); }
//  public static Value mul(Value a, Value b) { return val(a.data*b.data); }
//
//  // subtract
//  public Value subtract(Value o) { return val(data-o.data); }
//  public Value sub(Value o) { return val(data-o.data); }
//  public static Value subtract(Value a, Value b) { return val(a.data-b.data); }
//  public static Value sub(Value a, Value b) { return val(a.data-b.data); }
//
//  // square
//  public Value sq() { return val(data*data); }
//  public static Value sq(Value a) { return val(a.data*a.data); }
//
//
//  // comparison
//  public boolean lessThan(Value o) { return data<o.data; }
//  public static boolean lessThan(Value a,Value b) { return a.data<b.data; }
//
//  // negate
//  public Value neg() { return val(-data); }
//  public static Value neg(Value a) { return val(-a.data); }
//
//  // reciprocal
//  public Value recip() { return val(1/data); }
//
//  // toString
//  @Override public String toString() { return ((Double)data).toString(); }
}
