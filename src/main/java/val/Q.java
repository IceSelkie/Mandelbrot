package val;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Q
{
  private static final String PRECISION = "10000000000000000";
  public static final String X = PRECISION+"0000000000000000";
  public static final BigInteger HALFPRECISION = new BigInteger("100000000");
  public static final BigDecimal BIGPRECISION = new BigDecimal(PRECISION);
  public static final BigDecimal BIGPRECISIONX = new BigDecimal(X);
  public BigInteger n, d;

  public Q(BigInteger n, BigInteger d)
  {
    this.n = n;
    this.d = d;
  }

  public Q(long n, long d)
  {
    this.n = new BigInteger(n+"");
    this.d = new BigInteger(d+"");
  }

  public Q m(Q o)
  {
    return new Q(n.multiply(o.n), d.multiply(o.d));
  }

  public Q m(int coeff)
  {
    return new Q(n.multiply(new BigInteger(coeff+"")), d);
  }

  public Q a(Q o)
  {
    return new Q(n.multiply(o.d).add(o.n.multiply(d)), d.multiply(o.d));
  }

  public Q s(Q o)
  {
    return new Q(n.multiply(o.d).subtract(o.n.multiply(d)), d.multiply(o.d));
  }

  public int hashCode()
  {
    return new Integer(n.hashCode() ^ d.hashCode()).hashCode();
  }

  public Q pow(int ex)
  {
    if (ex == 0)
      return new Q(1, 1);
    Q base = this;
    if (ex < 0) {
      base = reci();
      ex = Math.abs(ex);
    }
    Q ret = base;

    while (ex-- > 1)
      ret = ret.m(base);
    return ret;
  }

  public Q reci()
  {
    return new Q(d, n);
  }

  @Override
  public String toString()
  {
    if (d.equals(BigInteger.ZERO))
      return n+"/"+d;

    BigDecimal localPrecision;
    if (HALFPRECISION.multiply(n).compareTo(d) < 0)
      localPrecision =BIGPRECISIONX;
    else
      localPrecision = BIGPRECISION;
    return new BigDecimal(n).multiply(localPrecision).divide(new BigDecimal(d), BigDecimal.ROUND_HALF_UP).divide(localPrecision).toString();
  }

  @Deprecated
  public static Q trot(int pow)
  {
    final Q trot = q(3330, 3107);
    return trot.pow(pow);
  }

  @Deprecated
  public static Q centi_rot(int pow)
  {
    final Q hrot = q(1882, 1869);
    return hrot.pow(pow);
  }

  @Deprecated
  public static Q milli_rot(int pow)
  {
    final Q hrot = q(7216, 7211);
    return hrot.pow(pow);
  }

  @Deprecated
  public static Q rot_to_milli(double pow)
  {
    return trot((int)(pow%10*10)).m(centi_rot((int)(pow*10%10*10))).m(milli_rot((int)(pow*100%10*10)));
  }

  public static Q srot(double pow)
  {
    //System.out.println("SROT -> 2^"+(((int)Math.round(64*pow))/64)+" (1/64)^"+(((int)Math.round(64*pow))%64)+" ("+(pow*64)+") ("+pow+")");
    return q(2).pow(((int)Math.round(64*pow))/64).m(srot(((int)Math.round(64*pow))%64));
  }

  public static Q srot(int pow)
  {
    final Q srot = q(557, 551);
    return srot.pow(pow);
  }

  public static Q q(long n, long d)
  {
    return new Q(n, d);
  }

  public static Q q(long n)
  {
    return new Q(n, 1);
  }

  public static Q q(String num)
  {
    //                                        1        1   2      2     3 3 4      4   5      5      6  6   7      7  8        8
    Matcher numRegex = Pattern.compile("(?:(?:(-?[0-9]+)\\/([0-9]+))|(?:(-)?([0-9]+)\\.([0-9]+)?)|(?:(-?)\\.([0-9]+))|(-?[0-9]+))").matcher(num);
    if (!numRegex.matches())
      throw new IllegalArgumentException("Number cannot be parsed as Quotient: Expected \"###/###\", \"###.###\", \"###.\", \"###\".");
    if (numRegex.group(1) != null)
      return new Q(new BigInteger(numRegex.group(1)), new BigInteger(numRegex.group(2)));
    if (numRegex.group(4) != null) {
      if (numRegex.group(5) == null)
        return new Q(new BigInteger((numRegex.group(3) == null ? "" : "-")+numRegex.group(4)), BigInteger.ONE);
      BigInteger d = BigInteger.TEN.pow(numRegex.group(5).length());
      BigInteger n = new BigInteger(numRegex.group(4)).multiply(d).add(new BigInteger(numRegex.group(5)));
      if (numRegex.group(3) != null)
        n = n.negate();
      return new Q(n, d);
    }
    if (numRegex.group(7) != null) {
      return new Q(new BigInteger(numRegex.group(6)+numRegex.group(7)), BigInteger.TEN.pow(numRegex.group(7).length()));
    }
    if (numRegex.group(8) != null)
      return new Q(new BigInteger(numRegex.group(8)), BigInteger.ONE);
    throw new IllegalStateException("Value parsing reached unreachable statement.");
  }
}
