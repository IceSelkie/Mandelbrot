package mandelbrot;

import val.Q;
import val.QP;
// primativedouble
// bigdouble
import val.bigdouble.Value;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static val.Q.*;
import static val.QP.qp;
import static val.bigdouble.Value.val;

public class Mandelbrot
{
  public static double COLORRATE = 1D/4D;
  public static final int TILESIZE = 150; // 150
  public static final int DEPTH = 2048*4; // 2048  render 8x
  public static final int ANTIALIASING = 1; // 1 render 16x
  public static final int MAXTHREADS = 4*8+1;
  public static final double ZOOMSCALE = 1D/8; // render 8

  private volatile Integer threadCount = 0; private synchronized int getThreads(){ return threadCount;} private synchronized void addThread(){ if (threadCount >=MAXTHREADS) System.err.println("Attempting to create a thread exceeding thread limit!"); threadCount++;} private synchronized void remThread(){ threadCount--;} private synchronized boolean canStartNewThread(){return threadCount<MAXTHREADS;}
  private volatile List<Thread> threads = Collections.synchronizedList(new ArrayList<Thread>(MAXTHREADS));
  HashMap<HashableView, Color[]> calculated = new HashMap<>(20);

  Display.WindowSize display_size;
  final int render_display_ratio = 4;
  final Display.WindowSize render_size = new Display.WindowSize(800*render_display_ratio,600*render_display_ratio);
  Display.WindowSize current_size;

  static boolean julia = false;
  String juliasave_scale, juliasave_center_x, juliasave_center_y;
  static Value juliacx;
  static Value juliacy;

  public static final Value FOUR = Value.FOUR;

  // Original
//  double scale = -7.5;
//  QP center = qp(q(-1, 2), q(0, 1));

  // ??
  //double scale = -20.5;
  //QP center = qp(q(81448, 49550959), q(-24170803,29388151));

  // 00 Cool spikes
  //double scale = -19.5;
  //QP center = qp(new Q(new BigInteger("-2301783278655076632811585394649140808770420500066574794752"),new BigInteger("15459423034510202420813247184330822928167207450548633600000")),new Q(new BigInteger("-7929977679099751430849043327089856342822712822632586149888"),new BigInteger("7729711517255101210406623592165411464083603725274316800000")));

  // 01 Arcing Zoom
  //double scale = -29.0;
  //QP center = qp(new Q(new BigInteger("-85241514145487948761243"), new BigInteger("572479338973652582400000")), new Q(new BigInteger("-7047730329760827785407283"), new BigInteger("6869752067683830988800000")));

  // 02
  double scale = -46.0;
//  double scale = -41;
  QP center = qp(new Q(new BigInteger("-824405337713456342058634333124188673418821894144"), new BigInteger("5536680432649312569992785998100296189031219200000")), new Q(new BigInteger("-68161465287625812021748692727096276126856290238464"), new BigInteger("66440165191791750839913431977203554268374630400000")));

  //Now centered on (-0.1488988479182952 + -1.0259075228224855 * i) (-156351065007566805536679717902520180585184297051059600526282234912293357272305181740113695589501876635357821471771652298663040820339068108800000/1050048856612784811624118896102900188226486902730961598736571103158496705677236567292605696792464875546003256803410319985501278977392640000000000 + i * -12927036255962464080151345488248209469761598172246797660221691866872273692602486962073929305996030740650276902171941681540968122573928372633600000/12600586279353417739489426753234802258717842832771539184838853237901960468126838807511268361509578506552039081640923839826015347728711680000000000)
  //double scale = -55.0;
  //QP center = qp(new Q(new BigInteger("-156351065007566805536679717902520180585184297051059600526282234912293357272305181740113695589501876635357821471771652298663040820339068108800000"), new BigInteger("1050048856612784811624118896102900188226486902730961598736571103158496705677236567292605696792464875546003256803410319985501278977392640000000000")), new Q(new BigInteger("-12927036255962464080151345488248209469761598172246797660221691866872273692602486962073929305996030740650276902171941681540968122573928372633600000"), new BigInteger("12600586279353417739489426753234802258717842832771539184838853237901960468126838807511268361509578506552039081640923839826015347728711680000000000")));

  // ## name
  //double scale = -13.5;
  //QP center = qp(new Q(new BigInteger(""), new BigInteger("")), new Q(new BigInteger(""), new BigInteger("")));

//  double scale = -49.5;
//  QP center = qp(new Q(new BigInteger("-19854057535354568"),BigInteger.TEN.pow(16)),new Q(new BigInteger("-00000260443807927"),BigInteger.TEN.pow(16)));


  public static void main(String[] args)
  {
//    if (args.length!=0)
//      Headless.headless(args);
//    else
      new Display().run();
  }

  public void doClick()
  {
    Point pt = Display.getCursorLocationOrigin(display_size);
    Q scaleFactor = srot(scale);
    center = qp(center.x.a(q(pt.x - display_size.w / 2).m(scaleFactor)), center.y.s(q(display_size.h / 2 - pt.y).m(scaleFactor)));

    clearThreads();
    calculated.clear();

    System.out.printf("%sNow centered on (%s + %s * i) (%s/%s + i * %s/%s)%s", "\n", center.x, center.y, center.x.n, center.x.d, center.y.n, center.y.d,"\n");
    s = false;
    loop = false;
  }

  private static boolean s = false;
  private static boolean loop = false;
  private static boolean notice = false;
  private static int loopindex = 0;
  public void keyPress(int key, int action)
  {
    if (action==GLFW_RELEASE)
    {
      if (key == GLFW_KEY_H)
      {
        System.out.println("Mandelbrot Render Help:");
        System.out.println("Press the following keys for the following actions.");
        System.out.println("  '+' - Increase. Increase zoom/zoom in. Also the '=' key.");
        System.out.println("  '-' - Decrease. Decrease zoom/zoom out.");
        System.out.println("  's' - Save the currently rendered image. Note: Render must be complete or a crash may occur.");
        System.out.println("  'w' - Write. Quick-write the current configuration to a file for a later quick load. Use 'r' to read/load.");
        System.out.println("  'r' - Read. Quick-read the current configuration from a file to load a quick save from before. Use 'w' to write/save.");
        System.out.println("  'l' - Loop. Start a loop zooming in from the current view. Can be used to cache rendered frames. If s was the last key pressed, it will save the render at the end of each frame.");
        System.out.println("  'z' - Zoom. Resets the zoom to the default zoom of -7.5. If currently -7.5, will set the zoom level to -50 (very deep).");
        System.out.println("  'v' - Viewframe. Switches the rendering viewframe between screen and huge. Huge is "+render_display_ratio+"x larger on each dimension than the original screen size.");
        System.out.println("  'j' - Julia. Switches between rendering the mandelbrot and the julia set at a given point. Julia's view will be reset each time.");
//        System.out.println("  'k' - Action.");
      }
      // Plus -> Zoom In
      if (key == GLFW_KEY_EQUAL || key == GLFW_KEY_KP_ADD)
        zoom(-ZOOMSCALE);
      // Minus -> Zoom Out
      if (key == GLFW_KEY_MINUS|| key == GLFW_KEY_KP_SUBTRACT)
        zoom(ZOOMSCALE);
      // S -> Save Image
      if (key== GLFW_KEY_S)
      {
        s = true;
        String filename = ("render/"+(julia ?"Julia":"Mandelbrot")+"Render"+"t"+System.currentTimeMillis()/1000)+"_"+center.x+"+"+center.y+"i"+"_"+"Zoom"+scale+"_"+"CLR"+COLORRATE+"_"+"DPTH"+DEPTH+"_"+current_size.w+"x"+current_size.h+"_"+"AA"+ANTIALIASING+".png";
        Headless.saveImage(filename, current_size.w, current_size.h,precalculated);
      }

      String latestFile = "last.mandel";
      // W -> Write View Parameters To File
      if (key==GLFW_KEY_W)
      {
        System.out.println("Starting text file location save at: "+latestFile);
        try {
          FileWriter fw = new FileWriter(latestFile);
          if (!julia)
          {
            fw.write(scale+"\n");
            fw.write(center.x+"\n");
            fw.write(center.y+"\n");
            fw.write(julia+"");
          }
          else
          {
            fw.write(juliasave_scale+"\n");
            fw.write(juliasave_center_x+"\n");
            fw.write(juliasave_center_y+"\n");
            fw.write(julia+"\n");
            fw.write(scale+"\n");
            fw.write(center.x+"\n");
            fw.write(center.y.toString());
          }
          fw.close();
          System.out.println("Location data saved as text.");
        } catch (IOException e)
        {
          System.err.println("Unable to save location.");
        }
      }
      // R -> Read View Parameters From File
      if (key==GLFW_KEY_R)
      {
        loop = false;
        s = false;
        try {
          Scanner scanner = new Scanner(new File(latestFile));
          scale=new Double(scanner.nextLine());
          center = qp(q(scanner.nextLine()),q(scanner.nextLine()));
          if (scanner.hasNext())
          {
            julia = Boolean.parseBoolean(scanner.nextLine());
            if (julia) {
              juliasave_scale = ((Object)scale).toString();
              juliacx = val(juliasave_center_x=center.x.toString());
              juliacy = val(juliasave_center_y=center.y.toString());
              scale=new Double(scanner.nextLine());
              center = qp(q(scanner.nextLine()),q(scanner.nextLine()));
            }
          }
          clearThreads();
          calculated.clear();
          System.out.printf("%sNow centered on (%s + %s * i) (%s/%s + i * %s/%s)%s", "\n", center.x, center.y, center.x.n, center.x.d, center.y.n, center.y.d,"\n");
          zoom(0);
        } catch (IOException e)
        {
          System.err.println("Unable to save location.");
        }
      }
      // L -> Start Render Loop
      // See S. If last action was save, loop will save at each iteration.
      if (key==GLFW_KEY_L)
      {
        loop = !loop;
        loopindex = 0;
      }
      // Z -> Zoom Reset (Zooms out, or if all the way zoomed out, zoom all the way in.)
      if (key==GLFW_KEY_Z)
      {
        if (scale!=-7.5)
          scale = -7.5;
        else
          scale = -50;
        zoom(0);
      }
      if (key==GLFW_KEY_V)
      {
        if (current_size == display_size)
          current_size = render_size;
        else
          current_size = display_size;
        clearThreads();
        calculated.clear();
      }
      if (key==GLFW_KEY_J)
      {
        if (julia = !julia)
        {
          juliacx = val(center.x.toString());
          juliacy = val(center.y.toString());
          juliasave_scale = ((Object)scale).toString();
          juliasave_center_x = ((Object)center.x).toString();
          juliasave_center_y = ((Object)center.y).toString();
          //center = qp(q(0),q(0));
          //scale = -7.5;
        }
        else
        {
          scale=new Double(juliasave_scale);
          center = qp(q(juliasave_center_x),q(juliasave_center_y));
          juliasave_scale = juliasave_center_x = juliasave_center_y = null;
        }
        clearThreads();
        calculated.clear();
        System.out.printf("%sNow rendering Julia set. c= (%s + %s * i) (%s/%s + i * %s/%s)%s", "\n", center.x, center.y, center.x.n, center.x.d, center.y.n, center.y.d,"\n");
        System.out.printf("%sStill centered on (%s + %s * i) (%s/%s + i * %s/%s)%s", "\n", center.x, center.y, center.x.n, center.x.d, center.y.n, center.y.d,"\n");
      }

      if (key!=GLFW_KEY_S && key!=GLFW_KEY_L)
      {
        s = false;
      }
    }
  }

  private void zoom(double zoomscale)
  {
    scale += zoomscale;
    clearThreads();
    System.out.println("The scale is now: " + scale + " (Pixel Size: "+srot(scale)+")");
  }

  Color[] precalculated = null;
  long startTime;
  public void display()
  {
    if (!Display.w.equals(this.display_size))
    {
      if (current_size == display_size) {
        calculated.clear();
        clearThreads();
      }
      this.display_size = Display.w;
    }
    if (current_size ==null)
      current_size = display_size;
    HashableView hv = new HashableView(current_size, center, scale);
    int hvhc = hv.hashCode();

    precalculated = null;
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

    if (precalculated == null || (getThreads()==0 && cont(precalculated,null))) {
      startTime=System.currentTimeMillis();
      calculate(hv);
    }
    else
      display(precalculated); //TODO
    if (precalculated!=null && !cont(precalculated,null)) {
      if(!notice)
      {
        System.out.println("Done! (frame complete in "+(System.currentTimeMillis()-startTime)/1000D+"s)");
        notice = true;
      }
      if (loop) {
        if (s) {
          String filename = ("render/series/MandelbrotRender"+(loopindex++)+".png");
          Headless.saveImage(filename, display_size.w, display_size.h, precalculated);
          calculated.clear();
        }
        zoom(-ZOOMSCALE);
      }
    }
    else
      notice = false;
  }

  public static boolean cont (Object[] data, Object value)
  {
    for (Object o : data)
      if (o==value)
        return true;
    return false;
  }

  // TODO
  public void display(Color[] data)
  {
    glPointSize(5f);
    //float mod = Math.min((float)display.w/use.w,(float)display.w/use.w);
    for (int y = 0; y < display_size.h; y++)
      for (int x = 0; x < display_size.w; x++)
      {
        if (current_size.w * y + x >= data.length)
          continue;
        Color c = data[current_size.w * y + x];
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
    Color[] clrset = new Color[hv.size.w*hv.size.h];
    calculated.put(hv, clrset);
    Thread runner = new Thread(()->
    {
      Q scaleFactor = srot(scale).reci().m(hv.size==render_size?render_display_ratio:1).reci();
      Q scaleFactorSmall = scaleFactor.m(q(1,ANTIALIASING));
      HashSet<int[]> tiles = new HashSet<>();
      for (int y = -hv.size.h/2; y < hv.size.h/2; y+=TILESIZE)
        for (int x = -hv.size.w/2; x < hv.size.w/2; x+=TILESIZE)
        {
          int[] toPut = new int[]{x, y, Display.normalizeInt(x + TILESIZE - 1, -hv.size.w / 2, hv.size.w / 2 - 1), Display.normalizeInt(y + TILESIZE - 1, -hv.size.h / 2, hv.size.h / 2 - 1)};
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
              int yPositionOffset = (y + hv.size.h / 2) * hv.size.w + hv.size.w / 2;
              for (int x = workTile[0]; x <= workTile[2]; x++)
              {
                Q qx = hv.pt.x.a(scaleFactor.m(x));
                //long steps = -1;
                int rate = ANTIALIASING*ANTIALIASING;
                Color clrave = Color.BLACK;
                try {
                  long[] d = new long[ANTIALIASING*ANTIALIASING];
                  for (int y2 = 0; y2 < ANTIALIASING; y2++)
                    for (int x2 = 0; x2 < ANTIALIASING; x2++)
                      // Decimal Type
                      d[y2*ANTIALIASING+x2] = countStepsValue(val(qx.a(scaleFactorSmall.m(x2)).toString()), val(qy.a(scaleFactorSmall.m(y2)).toString()), DEPTH);
                  // Quotient Type
                  //d[y2*ANTIALIASING+x2] = countStepsValue(val(qx.n,qx.d), val(qy.n,qy.d), DEPTH);

                  int red = 0, green = 0, blue = 0;
                  for (int i = 0; i < ANTIALIASING*ANTIALIASING; i++)
                    if (d[i] != -1) {
                      Color loc = Display.hsb4ToColor((int)(d[i]*COLORRATE%360L), 90, rate*90, 255);
                      red += loc.getRed();
                      green += loc.getGreen();
                      blue += loc.getBlue();
                    }
                  clrave = new Color(red/(ANTIALIASING*ANTIALIASING),green/(ANTIALIASING*ANTIALIASING),blue/(ANTIALIASING*ANTIALIASING));
                } catch (Exception e)
                {
                  System.out.println("{"+qx.toString()+","+qy.toString()+"}");
                }
                synchronized (clrset)
                {
                  clrset[yPositionOffset+x] = clrave;
                  //if (steps == -1)
                  //  clrset[yPositionOffset + x] = Color.BLACK;
                  //else
                  //  clrset[yPositionOffset + x] = Display.hsb4ToColor((int)steps, 90, rate*90, 255);
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
    Value zx = cx, zy = cy;
    Value temp;

    Value zxS = zx.mul(zx), zyS = zy.mul(zy);

    long steps = 1;
    if (!julia)
    while (steps < maxSteps && zxS .add (zyS) .lessThan (FOUR))
    {
      temp = zxS .sub (zyS) .add (cx);
      zy = (zx .add (zy)).sq() .sub (zxS) .sub (zyS) .add (cy);
      zx = temp;
      steps++;
      zxS = zx.mul(zx);
      zyS = zy.mul(zy);
    }
    else
      while (steps < maxSteps && zxS .add (zyS) .lessThan (FOUR))
      {
        temp = zxS .sub (zyS) .add (juliacx);
        zy = (zx .add (zy)).sq() .sub (zxS) .sub (zyS) .add (juliacy);
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

  public static class HashableView
  {
    Display.WindowSize size;
    QP pt;
    double scl;

    public HashableView(Display.WindowSize size, QP point, double scale)
    {
      this.size = size;
      pt = point;
      scl = scale;
    }

    public int hashCode()
    {
      return Arrays.hashCode(new Object[]{size, pt, scl});
      //return new Integer(pt.hashCode() ^ ((Double) scl).hashCode()).hashCode();
    }
  }
}
