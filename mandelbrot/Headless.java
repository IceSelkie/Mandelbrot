//package mandelbrot;
//
//import val.Q;
//
//import javax.imageio.ImageIO;
//import java.awt.*;
//import java.awt.image.*;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.math.BigInteger;
//import java.util.*;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import static mandelbrot.Mandelbrot.ANTIALIASING;
//import static mandelbrot.Mandelbrot.MAXTHREADS;
//import static val.Q.*;
//import static val.primativedouble.Value.val;
//
//public class Headless
//{
//  //  Match a #/# or #. or #.# or .# or #
//  public static final String regex_val_quo = "(?:(?:-?[0-9]+\\/[0-9]+)|(?:-?[0-9]+\\.(?:[0-9]+)?)|(?:-?\\.[0-9]+)|(?:-?[0-9]+))";
//  //  Match a #. or #.# or .# or #
//  public static final String regex_val_doub = "(?:(?:-?[0-9]+\\.(?:[0-9]+)?)|(?:-?\\.[0-9]+)|(?:-?[0-9]+))";
//  private volatile Integer threadCount = 0;
//
//  private synchronized int getThreads(){ return threadCount;} private synchronized void addThread(){ if (threadCount >=MAXTHREADS) System.err.println("Attempting to create a thread exceeding thread limit!"); threadCount++;} private synchronized boolean canStartNewThread(){cleanThreads(); return threadCount<MAXTHREADS;}
//  private volatile List<Thread> threads = Collections.synchronizedList(new ArrayList<Thread>(MAXTHREADS));private synchronized void cleanThreads() {Iterator<Thread> i = threads.iterator(); while (i.hasNext()) if (!i.next().isAlive()) i.remove(); threadCount = threads.size(); }
//
//
//  // YEEK
//  public static void headless(String[] args)
//  {
//    if (args.length < 4 || args.length > 7)
//      throw new IllegalArgumentException("Four to Seven arguments expected: ImageSize CoordinateReal CoordinateImaginary ScaleDepth (Steps) [HueChangeRate] [AntiAliasingDegree]");
//
//    int width, height;
//    {
//      Matcher imageSize = Pattern.compile("^([0-9]+)x([0-9]+)$").matcher(args[0]);
//      if (!imageSize.matches())
//        throw new IllegalArgumentException("ImageSize argument expected format \"WWWxHHH\" but received \""+args[0]+"\" instead.");
//      width = new Integer(imageSize.group(1));
//      height = new Integer(imageSize.group(2));
//      if (width == 0 || height == 0)
//        throw new IllegalArgumentException("ImageSize cannot be zero. Received \""+width+"x"+height+"\"");
//    }
//
//    Q realA, realB, realS;
//    {
//      Matcher coordinateReal = Pattern.compile("^("+regex_val_quo+")(?::("+regex_val_quo+")(?::("+regex_val_quo+"))?)?$").matcher(args[1]);
//      if (!coordinateReal.matches())
//        throw new IllegalArgumentException("CoordinateReal argument expected format \"AAA\" for a constant value, \"AAA:BBB\" to vary from A to B, or \"AAA:BBB:SSS\" to vary from A to B with a step size of S. Values can be whole, decimals, or quotients.");
//      String A = coordinateReal.group(1);
//      String B = coordinateReal.group(2);
//      String S = coordinateReal.group(3);
//      realA = q(A);
//      realB = (B == null) ? realA : q(B);
//      realS = (S == null) ? null : q(S);
//    }
//
//    Q imagA, imagB, imagS;
//    {
//      Matcher coordinateImaginary = Pattern.compile("^("+regex_val_quo+")(?::("+regex_val_quo+")(?::("+regex_val_quo+"))?)?$").matcher(args[2]);
//      if (!coordinateImaginary.matches())
//        throw new IllegalArgumentException("CoordinateImaginary argument expected format \"AAA\" for a constant value, \"AAA:BBB\" to vary from A to B, or \"AAA:BBB:SSS\" to vary from A to B with a step size of S. Values can be whole, decimals, or quotients.");
//      imagA = q(coordinateImaginary.group(1));
//      imagB = (coordinateImaginary.group(2) == null) ? imagA : q(coordinateImaginary.group(2));
//      imagS = (coordinateImaginary.group(3) == null) ? null : q(coordinateImaginary.group(3));
//    }
//
//    Double scalA, scalB, scalS;
//    {
//      Matcher scaleDepth = Pattern.compile("^("+regex_val_doub+")(?::("+regex_val_doub+")(?::("+regex_val_doub+"))?)?$").matcher(args[3]);
//      if (!scaleDepth.matches())
//        throw new IllegalArgumentException("ScaleDepth argument expected format \"AAA\" for a constant value, \"AAA:BBB\" to vary from A to B, or \"AAA:BBB:SSS\" to vary from A to B with a step size of S. Values can be whole or decimals.");
//      scalA = new Double(scaleDepth.group(1));
//      scalB = (scaleDepth.group(2) == null) ? scalA : new Double(scaleDepth.group(2));
//      scalS = (scaleDepth.group(3) == null) ? null : new Double(scaleDepth.group(3));
//    }
//
//    int steps;
//    double colorrate = Mandelbrot.COLORRATE;
//    int antialiasing = 1;
//    int index = 4;
//    Q realD, imagD;
//    double scalD;
//    if (realS == null && imagS == null && scalS == null) {
//      if (args.length > index)
//        try {
//          steps = new Integer(args[index++]);
//          if (steps <= 0) throw new Exception();
//        } catch (Exception e) {
//          throw new IllegalArgumentException("Steps argument expected whole number (greater than zero), received \""+args[index-1]+"\".");
//        }
//      else
//        throw new IllegalArgumentException("Four to Seven arguments expected: ImageSize CoordinateReal CoordinateImaginary ScaleDepth (Steps) [HueChangeRate] [AntiAliasingDegree]");
//
//      if (steps == 1 && realA != realB)
//        throw new IllegalArgumentException("Steps count received is 1, but CoordinateReal is to be varied!");
//      if (steps == 1 && imagA != imagB)
//        throw new IllegalArgumentException("Steps count received is 1, but CoordinateImaginary is to be varied!");
//      if (steps == 1 && scalA != scalB)
//        throw new IllegalArgumentException("Steps count received is 1, but ScaleDepth is to be varied!");
//      realD = (realB.s(realA)).reci().m(steps-1).reci();
//      imagD = (imagB.s(imagA)).reci().m(steps-1).reci();
//      scalD = (scalB-scalA)/(steps-1);
//    }
//    else {
//      steps = -1;
//      realD = q(0);
//      imagD = q(0);
//      scalD = 0;
//      if (realS != null) {
//        realD = realS;
//        if (realS.n.equals(BigInteger.ZERO))
//          throw new IllegalArgumentException("CoordinateReal's step size cannot be zero!");
//        Q temp = realB.s(realA).m(realS.reci());
//        steps = new Integer(temp.n.divide(temp.d).toString())+1;
//        if (steps <= 0)
//          throw new IllegalArgumentException("CoordinateReal's step size is going the wrong way and should have the opposite sign.");
//      }
//      if (imagS != null) {
//        imagD = imagS;
//        if (imagS.n.equals(BigInteger.ZERO))
//          throw new IllegalArgumentException("CoordinateImaginary's step size cannot be zero!");
//        Q temp = imagB.s(imagA).m(imagS.reci());
//        int newSteps = new Integer(temp.n.divide(temp.d).toString())+1;
//        if (newSteps <= 0)
//          throw new IllegalArgumentException("CoordinateImaginary's step size is going the wrong way and should have the opposite sign.");
//        if (steps != -1 && steps != newSteps)
//          throw new IllegalArgumentException("CoordinateImaginary's step size does not match the previously set step size! Previous step count: \""+steps+"\" New step count: \""+newSteps+"\"");
//        steps = newSteps;
//      }
//      if (scalS != null) {
//        scalD = scalS;
//        if (scalS == 0)
//          throw new IllegalArgumentException("ScaleDepth's step size cannot be zero!");
//        int newSteps = (int)((scalB-scalA)/scalS)+1;
//        if (newSteps <= 0)
//          throw new IllegalArgumentException("ScaleDepth's step size is going the wrong way and should have the opposite sign.");
//        if (steps != -1 && steps != newSteps)
//          throw new IllegalArgumentException("ScaleDepth's step size does not match the previously set step size! Previous step count: \""+steps+"\" New step count: \""+newSteps+"\"");
//        steps = newSteps;
//      }
//    }
//    if (args.length > index)
//      try {
//        colorrate = new Double(args[index++]);
//        if (colorrate == 0) throw new Exception();
//      } catch (Exception e) {
//        throw new IllegalArgumentException("HueChangeRate argument expected a number (non-zero), received \""+args[index-1]+"\".");
//      }
//    if (args.length > index)
//      try {
//        antialiasing = new Integer(args[index++]);
//        if (antialiasing <= 0) throw new Exception();
//      } catch (Exception e) {
//        throw new IllegalArgumentException("AntiAliasingDegree argument expected whole number (greater than zero), received \""+args[index-1]+"\". Be careful of this number, increases time at On^2 rate. Recommended 1 for fast, 2 or 4 for medium, and at most 16 for high quality render.");
//      }
//
//    if (steps==1)
//      System.out.println("render("+width+","+height+","+realA+","+realB+","+imagA+","+imagB+","+scalA+","+scalB+","+steps+","+Mandelbrot.DEPTH+","+colorrate+","+antialiasing+");");
//    else
//      System.out.println("render("+width+","+height+","+realA+","+realB+","+realD+","+imagA+","+imagB+","+imagD+","+scalA+","+scalB+","+scalD+","+steps+","+Mandelbrot.DEPTH+","+colorrate+","+antialiasing+");");
//
//    if (steps==1)
//      new Headless().render(width, height, realA, imagA, scalA, Mandelbrot.DEPTH, colorrate, antialiasing);
//      else
//    for (int i = 0; i < steps; i++) {
//      new Headless().render(width, height, realA.a(realD.m(i)), imagA.a(imagD.m(i)), scalA+scalD*i, Mandelbrot.DEPTH, colorrate, antialiasing);
//    }
//  }
//
//
//  HashMap<Integer, Color[]> data = new HashMap<>();
//  public void render(int width, int height,Q real, Q imag, double scale, int maxdepth, double colorrate, int antialiasing)
//  {
//    final int tilesize = Mandelbrot.TILESIZE;
//
//    data.put(0,new Color[width*height]);
//
//    //long threadid = System.nanoTime()%1000000;
//    //System.out.println(threadid+" started!");
//    Q scaleFactor = srot(scale);
//    Q scaleFactorSmall = scaleFactor.m(q(1, antialiasing));
//    HashSet<int[]> tiles = new HashSet<>();
//    for (int y = -height/2; y < height/2; y += tilesize)
//      for (int x = -width/2; x < width/2; x += tilesize) {
//        int[] toPut = new int[]{x, y, Display.normalizeInt(x+tilesize-1, -width/2, width/2-1), Display.normalizeInt(y+tilesize-1, -height/2, height/2-1)};
//        tiles.add(toPut);
//      }
//    Iterator<int[]> tileSet = tiles.iterator();
//    while (tileSet.hasNext()) {
//      if (canStartNewThread()) {
////TODO        CustomRecursiveTask crt = new CustomRecursiveTask(tileSet.next(),width,height,real,imag,scaleFactor,scaleFactorSmall,maxdepth,colorrate);
////TODO        Color[] tileData = ;
//      }
//      else
//        try { Thread.sleep(50); } catch (InterruptedException e) {}
//    }
//    while (true) {
//      try { Thread.sleep(1000); } catch (InterruptedException e) {}
//      int nullct = 0;
////        for (Color color : image) if (color == null) { nullct++; }
////      cleanThreads();
////      System.out.println("Completion: "+((int)((1-((double)nullct/image.length))*100))+"."+((int)((1-((double)nullct/image.length))*10000)%100)+"%. ("+getThreads()+" threads running)");
////      if (nullct == 0) break;
//      if (threadCount==0)
//        break;
//      else
//        System.out.println("Threads: "+threadCount);
//    }
//
////      saveImage("fn.png", width, height, image);
//  }
//
//  private Color[] doTile(int[] workTile, int width,int height, Q real, Q imag, Q scaleFactor, Q scaleFactorSmall, int maxdepth, double colorrate)
//  {
//    Color[] colors = new Color[width*height];
//    Thread worker = new Thread(() ->
//    {
//      for (int y = workTile[1]; y <= workTile[3]; y++) {
//        System.out.println(Arrays.hashCode(workTile)+ "->"+y);
//        Q qy = (imag.a(scaleFactor.m(y)));
//        int yPositionOffset = (y+height/2)*width+width/2;
//        for (int x = workTile[0]; x <= workTile[2]; x++) {
//          Q qx = real.a(scaleFactor.m(x));
//          long steps = -1;
//          int rate = ANTIALIASING*ANTIALIASING;
//          try {
//            long[] d = new long[ANTIALIASING*ANTIALIASING];
//            for (int y2 = 0; y2 < ANTIALIASING; y2++)
//              for (int x2 = 0; x2 < ANTIALIASING; x2++)
//                // Decimal Type
//                d[y2*ANTIALIASING+x2] = Mandelbrot.countStepsValue(val(qx.a(scaleFactorSmall.m(x2)).toString()), val(qy.a(scaleFactorSmall.m(y2)).toString()), maxdepth);
//            // Quotient Type
//            //d[y2*ANTIALIASING+x2] = countStepsValue(val(qx.n,qx.d), val(qy.n,qy.d), DEPTH);
//
//            for (int i = 0; i < ANTIALIASING*ANTIALIASING; i++)
//              if (d[i] != -1)
//                steps += d[i];
//              else
//                rate--;
//            if (rate != 0)
//              steps = (int)(((colorrate*(steps+1))/rate)%360);
//          } catch (Exception e) {
//            System.out.println("{"+qx.toString()+","+qy.toString()+"}");
//          }
//
//          Color[] image = data.get(0);
//          //System.out.println("Prepping Sync");
//          synchronized (image) {
//            System.out.println("Starting Sync");
//            if (steps == -1)
//              image[yPositionOffset+x] = Color.BLACK;
//            else
//              image[yPositionOffset+x] = Display.hsb4ToColor((int)steps, 90, rate*90, 255);
//            System.out.println("Finishing Sync");
//          }
//          System.out.println("Done with Sync");
//        }
//      }
//    });
//    threads.add(worker);
//    worker.start();
//    addThread();
//    return colors;
//  }
//
//  public static void saveImageAsText(String filename, int width, int height, Color[] data)
//  {
//    System.out.println("Starting text file save at: "+filename);
//    try {
//      FileWriter fw = new FileWriter(filename);
//      fw.write("{\"width\":"+width+",\"height\":"+height+",\"colors\":[");
//      for (int i = 0; i<data.length; i++) {
//        Color c = data[i];
//        fw.write("["+c.getRed()+","+c.getGreen()+","+c.getBlue()+"]"+(i==data.length-1?"":","));
//      }
//      fw.write("]}");
//      fw.close();
//      System.out.println("Image data saved as text.");
//    } catch (IOException e)
//    {
//      System.err.println("Unable to save image as text.");
//    }
//  }
//  public static void saveImage(String filename, int width, int height, Color[] data)
//  {
//    System.out.println("Starting image file save at: "+filename);
//    try {
//      int[] rgbs = new int[data.length];
//      for (int i = 0; i<rgbs.length; i++)
//        rgbs[i] = data[i].getRGB();
//      DataBuffer rgbData = new DataBufferInt(rgbs, rgbs.length);
//
//      WritableRaster raster = Raster.createPackedRaster(rgbData, width, height, width, new int[]{0xff0000, 0xff00, 0xff}, null);
//      ColorModel colorModel = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);
//      BufferedImage img = new BufferedImage(colorModel, raster, false, null);
//
//      ImageIO.write(img, "png", new File(filename));
//      System.out.println("Image file saved.");
//    } catch (IOException e)
//    {
//      System.err.println("Unable to save image.");
//    }
//  }
//}
