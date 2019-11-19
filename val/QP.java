package val;

public class QP
{
  public Q x, y;

  public QP(Q x, Q y)
  {
    this.x = x;
    this.y = y;
  }

  @Override
  public int hashCode()
  {
    return new Integer(x.hashCode() ^ y.hashCode()).hashCode();
  }

  @Override
  public String toString()
  {
    return "{"+x+", "+y+"}";
  }

  public static QP qp(Q x, Q y)
  {
    return new QP(x, y);
  }
}
