package val.eightybit;

public class Value
{
  private static final long MASK =        0x00000000FFFFFFFFL;
  private static final long MASK_TOPBIT = 0x0000000080000000L;
  private static final long MASK_LOWBIT = 0x0000000000000001L;
  private boolean negative;
  private int exponent;
  private long mantissa_big; // lower half only used
  private long mantissa_small; // lower half only used
  // Recombine with "(mantissa_big << 32) + mantissa_small"

//  public static final Value ZERO = val(0);
//  public static final Value ONE = val(1);
//  public static final Value TWO = val(2);
//  public static final Value FOUR = val(4);

  // create
  //public Value(double val) { this.data = val; }
  //public Value(String val) { this.data = new Double(val); }
  //public static Value val(double v) { return new Value(v); }
  //public static Value val(String v) { return new Value(v); }
  private Value(long mantissa_big, long mantissa_small, int exponent, boolean negative) { this.negative = negative;this.exponent = exponent;this.mantissa_big = mantissa_big;this.mantissa_small = mantissa_small; }
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
      exponent++;
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
      while ((mantissa_big & 0x8000000L)==0)
      {
        mantissa_big = mantissa_big<<1+mantissa_small>>31;
        mantissa_small = mantissa_small<<1&MASK;
        exponent--;
      }
    }
  };

  // add
  public Value add(Value o)
  {
    //if (o.equals(ZERO))
    //  return this;
    //if (this.equals(ZERO))
    //  return o;
    //if (negative^o.negative)
    //  return sub(o.negate());
    if (o.exponent>exponent)
      return o.add(this);

    // O is much smaller
    if (o.exponent+64<exponent)
      return this;

    // O is smaller
    long ret_big = this.mantissa_big + o.mantissa_big>>(exponent-o.exponent);
    long ret_small = this.mantissa_small + o.mantissa_big<<(32-(exponent-o.exponent))+o.mantissa_small>>(exponent-o.exponent);
    Value ret = new Value(ret_big, ret_small, exponent, negative);
    ret.fix();
    return ret;
  }
  public static Value add(Value a, Value b) { return val(a.data+b.data); }

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

  // toString
  @Override public String toString()
  {
    double mod = 1;
    double ret = 0;
    long mb = mantissa_big;
    for (int i = 0; i<32; i++)
    {
      if ((mb&MASK_TOPBIT)!=0)
        ret += mod;
      mod/=2;
      mb<<=1;
    }
    for (int i = 0; i<exponent;i++)
      ret*=2;
    for (int i = 0; i>exponent;i--)
      ret/=2;
    return ((Double)ret).toString();
  }
}
