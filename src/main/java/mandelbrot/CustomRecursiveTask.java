//package mandelbrot;
//
//import val.Q;
//
//import java.awt.*;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.List;
//import java.util.concurrent.ForkJoinTask;
//import java.util.concurrent.RecursiveTask;
//
//public class CustomRecursiveTask extends RecursiveTask<Integer>
//{
//  int[] workTile;
//  int width;
//  int height;
//  Q real;
//  Q imag;
//  Q scaleFactor;
//  Q scaleFactorSmall;
//  int maxdepth;
//  double colorrate;
//
//  private static final int THRESHOLD = 20;
//
//
//  public CustomRecursiveTask(int[] workTile, int width, int height, Q real, Q imag, Q scaleFactor, Q scaleFactorSmall, int maxdepth, double colorrate)
//  {
//    this.workTile = workTile;
//    this.width = width;
//    this.height = height;
//    this.real = real;
//    this.imag = imag;
//    this.scaleFactor = scaleFactor;
//    this.scaleFactorSmall = scaleFactorSmall;
//    this.maxdepth = maxdepth;
//    this.colorrate = colorrate;
//  }
//
//  @Override
//  protected Integer compute()
//  {
//    if (arr.length > THRESHOLD) {
//      return ForkJoinTask.invokeAll(createSubtasks())
//        .stream()
//        .mapToInt(ForkJoinTask::join)
//        .sum();
//    }
//    else {
//      return processing(arr);
//    }
//  }
//
//  private Collection<CustomRecursiveTask> createSubtasks()
//  {
//    List<CustomRecursiveTask> dividedTasks = new ArrayList<>();
//    dividedTasks.add(new CustomRecursiveTask(
//      Arrays.copyOfRange(arr, 0, arr.length/2)));
//    dividedTasks.add(new CustomRecursiveTask(
//      Arrays.copyOfRange(arr, arr.length/2, arr.length)));
//    return dividedTasks;
//  }
//
//  private Integer processing(int[] arr)
//  {
//    return Arrays.stream(arr)
//      .filter(a -> a > 10 && a < 27)
//      .map(a -> a*10)
//      .sum();
//  }
//}