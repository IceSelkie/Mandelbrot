import val.primativedouble.Value;

import java.awt.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static val.primativedouble.Value.FOUR;
import static val.primativedouble.Value.val;

public class Mandelbrot
{
  public static int COLORRATE = 5;
  public static final int TILESIZE = 25; // 150
  public static final int DEPTH = 4*2048; // 2048
  public static final int ANTIALIASING = 16; // 1
  public static final int MAXTHREADS = 4*8+1;
  public static final String PRECISION = "10000000000000000";
  private volatile Integer threadCount = 0; private synchronized int getThreads(){ return threadCount;} private synchronized void addThread(){ if (threadCount >=MAXTHREADS) System.err.println("Attempting to create a thread exceeding thread limit!"); threadCount++;} private synchronized void remThread(){ threadCount--;} private synchronized boolean canStartNewThread(){return threadCount<MAXTHREADS;}
  private volatile List<Thread> threads = Collections.synchronizedList(new ArrayList<Thread>(MAXTHREADS));
  HashMap<HashableView, Color[]> calculated = new HashMap<>(500);
  Display.WindowSize w;

  // Original
  //double scale = -7.5;
  //QP center = qp(q(-1, 2), q(0, 1));

  // ??
  //double scale = -20.5;
  //QP center = qp(q(81448, 49550959), q(-24170803,29388151));

  // 00 Cool spikes
  //double scale = -19.5;
  //QP center = qp(new Q(new BigInteger("-2301783278655076632811585394649140808770420500066574794752"),new BigInteger("15459423034510202420813247184330822928167207450548633600000")),new Q(new BigInteger("-7929977679099751430849043327089856342822712822632586149888"),new BigInteger("7729711517255101210406623592165411464083603725274316800000")));

  // 01 Arcing Zoom
  //double scale = -29.0;
  //QP center = qp(new Q(new BigInteger("-85241514145487948761243"), new BigInteger("572479338973652582400000")), new Q(new BigInteger("-7047730329760827785407283"), new BigInteger("6869752067683830988800000")));

  //Now centered on (-0.1488988479183 + -1.0259075228194 * i) (-824405337713456342058634333124188673418821894144/5536680432649312569992785998100296189031219200000 + i * -68161465287625812021748692727096276126856290238464/66440165191791750839913431977203554268374630400000)
  double scale = -45.0;
  QP center = qp(new Q(new BigInteger("-824405337713456342058634333124188673418821894144"), new BigInteger("5536680432649312569992785998100296189031219200000")), new Q(new BigInteger("-68161465287625812021748692727096276126856290238464"), new BigInteger("66440165191791750839913431977203554268374630400000")));

  //Now centered on (-0.1488988479182952 + -1.0259075228224855 * i) (-156351065007566805536679717902520180585184297051059600526282234912293357272305181740113695589501876635357821471771652298663040820339068108800000/1050048856612784811624118896102900188226486902730961598736571103158496705677236567292605696792464875546003256803410319985501278977392640000000000 + i * -12927036255962464080151345488248209469761598172246797660221691866872273692602486962073929305996030740650276902171941681540968122573928372633600000/12600586279353417739489426753234802258717842832771539184838853237901960468126838807511268361509578506552039081640923839826015347728711680000000000)
  //double scale = -55.0;
  //QP center = qp(new Q(new BigInteger("-156351065007566805536679717902520180585184297051059600526282234912293357272305181740113695589501876635357821471771652298663040820339068108800000"), new BigInteger("1050048856612784811624118896102900188226486902730961598736571103158496705677236567292605696792464875546003256803410319985501278977392640000000000")), new Q(new BigInteger("-12927036255962464080151345488248209469761598172246797660221691866872273692602486962073929305996030740650276902171941681540968122573928372633600000"), new BigInteger("12600586279353417739489426753234802258717842832771539184838853237901960468126838807511268361509578506552039081640923839826015347728711680000000000")));

  // ## name
  //double scale = -13.5;
  //QP center = qp(new Q(new BigInteger(""), new BigInteger("")), new Q(new BigInteger(""), new BigInteger("")));


  public static void main(String[] args)
  {
    if (args.length>=1)
      COLORRATE = new Integer(args[0]);
    if (COLORRATE<1)
      COLORRATE = 5;
    new Display().run();
  }

  public void doClick()
  {
    Point pt = Display.getCursorLocationOrigin(w);
    Q scaleFactor = q(2).pow((int) scale).m(trot((int) (scale * 10) % 10));
    center = qp(center.x.a(q(pt.x - w.w / 2).m(scaleFactor)), center.y.s(q(w.h / 2 - pt.y).m(scaleFactor)));

    clearThreads();
    calculated.clear();

    System.out.printf("%sNow centered on (%s + %s * i) (%s/%s + i * %s/%s)%s", "\n", center.x, center.y, center.x.n, center.x.d, center.y.n, center.y.d,"\n");
  }

  public void keyPress(int key, int action)
  {
    if (action==GLFW_RELEASE)
    {
      if (key == GLFW_KEY_EQUAL || key == GLFW_KEY_KP_ADD)
      {
        scale += .5 * -1;
        clearThreads();
      }
      if (key == GLFW_KEY_MINUS|| key == GLFW_KEY_KP_SUBTRACT)
      {
        scale += .5 * 1;
        clearThreads();
      }
      System.out.println("The scale is now: " + scale);
    }
  }
  public void display()
  {
    if (!Display.w.equals(this.w))
    {
      calculated.clear();
      clearThreads();
      this.w = Display.w;
    }
    HashableView hv = new HashableView(center, scale);
    int hvhc = hv.hashCode();

    Color[] precalculated = null;
    for (HashableView h : calculated.keySet())
      if (h.hashCode() == hvhc)
        precalculated = calculated.get(h);

    synchronized (threads)
    {
      ArrayList<Thread> toRem = new ArrayList<>();
      for (Thread thrd : threads)
        if (!thrd.isAlive())
        {
          toRem.add(thrd);
        }
      for (Thread thrd : toRem)
      {
        thrd.interrupt();
        threads.remove(thrd);
        remThread();
      }
    }

    if (precalculated == null || (getThreads()==0 && cont(precalculated,null)))
      calculate(hv);
    else
      display(precalculated);
  }

  public static boolean cont (Object[] data, Object value)
  {
    for (Object o : data)
      if (o==value)
        return true;
    return false;
  }

  public void display(Color[] data)
  {
    glPointSize(5f);
    for (int y = 0; y < w.h; y++)
      for (int x = 0; x < w.w; x++)
      {
        Color c = data[w.w * y + x];
        if (c != null)
          Display.setColor3(c);
        else
          glColor3f(.125f, .125f, .125f);
        glBegin(GL_POINTS);
        Display.doPointOr(x, y);
        glEnd();
      }
  }

  public void calculate(HashableView hv)
  {
    Color[] clrset = new Color[w.w*w.h];
    calculated.put(hv, clrset);
    Thread runner = new Thread(()->
    {
      Q scaleFactor = q(2).pow((int) scale).m(trot((int) (scale * 10) % 10));
      Q scaleFactorSmall = scaleFactor.m(q(1,ANTIALIASING));
      HashSet<int[]> tiles = new HashSet<>();
      for (int y = -w.h/2; y < w.h/2; y+=TILESIZE)
        for (int x = -w.w/2; x < w.w/2; x+=TILESIZE)
        {
          int[] toPut = new int[]{x, y, Display.normalizeInt(x + TILESIZE - 1, -w.w / 2, w.w / 2 - 1), Display.normalizeInt(y + TILESIZE - 1, -w.h / 2, w.h / 2 - 1)};
          tiles.add(toPut);
        }
      Iterator<int[]> tileSet= tiles.iterator();
      while (tileSet.hasNext())
      {
        if (canStartNewThread())
        {
          final int[] workTile = tileSet.next();
          Thread worker = new Thread(() ->
          {
            for (int y = workTile[1]; y <= workTile[3]; y++)
            {
              Q qy = (hv.pt.y.a(scaleFactor.m(y)));
              int yPositionOffset = (y + w.h / 2) * w.w + w.w / 2;
              for (int x = workTile[0]; x <= workTile[2]; x++)
              {
                Q qx = hv.pt.x.a(scaleFactor.m(x));
                long steps = -1;
                int rate = ANTIALIASING*ANTIALIASING;
                try {
                  long[] d = new long[ANTIALIASING*ANTIALIASING];
                  for (int y2 = 0; y2<ANTIALIASING; y2++)
                    for (int x2 = 0; x2<ANTIALIASING; x2++)
                      // Decimal Type
                      d[y2*ANTIALIASING+x2] = countStepsValue(val(qx.a(scaleFactorSmall.m(x2)).toString()), val(qy.a(scaleFactorSmall.m(y2)).toString()), DEPTH);
                      // Quotient Type
                      //d[y2*ANTIALIASING+x2] = countStepsValue(val(qx.n,qx.d), val(qy.n,qy.d), DEPTH);

                  for (int i = 0; i<ANTIALIASING*ANTIALIASING; i++)
                    if (d[i]!=-1)
                      steps+=d[i];
                    else
                      rate--;
                    if (rate!=0)
                      steps = ((COLORRATE*(steps+1)) / rate)%360;
                } catch (Exception e)
                {
                  System.out.println("{"+hv.pt.x.a(scaleFactor.m(x)).toString()+","+qy.toString()+"}");
                }
                synchronized (clrset)
                {
                  if (steps == -1)
                    clrset[yPositionOffset + x] = Color.BLACK;
                  else
                    clrset[yPositionOffset + x] = Display.hsb4ToColor((int)steps, 90, rate*90, 255);
                }
              }
            }
          });
          synchronized (threads)
          {
            threads.add(worker);
          }
          worker.start();
          addThread();
        }
        else
          try { Thread.sleep(50); } catch (InterruptedException e) {}
      }
    });

    synchronized (threads)
    {
      threads.add(runner);
    }
    runner.start();
    addThread();
  }

  public static long countStepsSimple(double cx, double cy, long maxSteps)
  {
    double zx = 0, zy = 0;
    double temp = 0;

    double zxS = zx * zx, zyS = zy * zy;

    long steps = 0;
    while (steps < maxSteps && zxS + zyS < 4)
    {
      temp = zxS - zyS + cx;
      zy = sq(zx + zy) - zxS - zyS + cy;
      zx = temp;
      steps++;
      zxS = zx * zx;
      zyS = zy * zy;
    }
    if (zxS + zyS < 4)
      steps++;
    return steps == maxSteps + 1 ? -1 : steps;
  }
  public static long countStepsValue(Value cx, Value cy, long maxSteps)
  {
    Value zx = Value.ZERO, zy = Value.ZERO;
    Value temp;

    Value zxS = zx.mul(zx), zyS = zy.mul(zy);

    long steps = 0;
    while (steps < maxSteps && zxS .add (zyS) .lessThan (FOUR))
    {
      temp = zxS .sub (zyS) .add (cx);
      zy = (zx .add (zy)).sq() .sub (zxS) .sub (zyS) .add (cy);
      zx = temp;
      steps++;
      zxS = zx.mul(zx);
      zyS = zy.mul(zy);
    }
    if (zxS .add (zyS) .lessThan (FOUR))
      steps++;
    return steps == maxSteps + 1 ? -1 : steps;
  }

  public void clearThreads()
  {
    if (getThreads()!=0)
    {
      synchronized (threads)
      {
        for (Thread t: threads)
        {
          t.interrupt();
          t.stop();
          remThread();
        }
        threads.clear();
      }
    }
  }

  public static double sq(double x)
  {
    return x*x;
  }
  public static Q q(long n, long d)
  {
    return new Q(n, d);
  }

  public static Q q(long n)
  {
    return new Q(n, 1);
  }

  public static Q trot(int pow)
  {
    final Q trot = q(3330,3107);
    return trot.pow(pow);
  }

  public static QP qp(Q x, Q y)
  {
    return new QP(x, y);
  }

  public static class HashableView
  {
    QP pt;
    double scl;

    public HashableView(QP point, double scale)
    {
      pt = point;
      scl = scale;
    }

    public int hashCode()
    {
      return new Integer(pt.hashCode() ^ ((Double) scl).hashCode()).hashCode();
    }
  }

  public static class Q
  {
    BigInteger n, d;

    public Q(BigInteger n, BigInteger d)
    {
      this.n = n;
      this.d = d;
    }

    public Q(long n, long d)
    {
      this.n = new BigInteger(n + "");
      this.d = new BigInteger(d + "");
    }

    public Q m(Q o)
    {
      return new Q(n.multiply(o.n), d.multiply(o.d));
    }

    public Q m(int coeff)
    {
      return new Q(n.multiply(new BigInteger(coeff + "")), d);
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
      if (ex < 0)
      {
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
      return new BigDecimal(n).multiply(new BigDecimal(PRECISION)).divide(new BigDecimal(d), BigDecimal.ROUND_HALF_UP).divide(new BigDecimal(PRECISION)).toString();
    }
  }

  public static class QP
  {
    Q x, y;

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
  }
}
