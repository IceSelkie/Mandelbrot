package mandelbrot;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Display
{
  private static long window;
  public static WindowSize w;
  private static Mandelbrot mandelbrot;

  public Display()
  {
    mandelbrot = new Mandelbrot();
  }

  public void run()
  {
    init();
    loop();
    terminate();
  }

  private void init()
  {
    // Prevent java.awt from interfering with this thread. If any awt stuff is called prior to this, the program will crash (Ex: {@code Color c = new Color(255,0,255);}).
    System.setProperty("java.awt.headless", "true");

    // Setup an error callback. The default implementation
    // will print the error message in System.err.
    GLFWErrorCallback.createPrint(System.err).set();

    // Initialize GLFW. Most GLFW functions will not work before doing this.
    if (!glfwInit())
      throw new IllegalStateException("Unable to initialize GLFW");

    // Configure GLFW
    glfwDefaultWindowHints(); // optional, the current window hints are already the default
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // whether the window can be resized.

    // Create the window
    window = glfwCreateWindow(800, 600, "Mandelbrot Zoom", NULL, NULL);
    if (window == NULL)
      throw new RuntimeException("Failed to create the GLFW window");

    registerCallbacks();

    // Get the thread stack and push a new frame
    try (MemoryStack stack = MemoryStack.stackPush())
    {
      IntBuffer pWidth = stack.mallocInt(1); // int*
      IntBuffer pHeight = stack.mallocInt(1); // int*

      // Get the window size passed to glfwCreateWindow
      glfwGetWindowSize(window, pWidth, pHeight);

      // Get the resolution of the primary monitor
      GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

      // Center the window
      glfwSetWindowPos(
          window,
          (vidmode.width() - pWidth.get(0)) / 2,
          (vidmode.height() - pHeight.get(0)) / 2
      );
    } // the stack frame is popped automatically

    // Make the OpenGL context current
    glfwMakeContextCurrent(window);
    // Enable v-sync
    glfwSwapInterval(1);

    // Make the window visible
    glfwShowWindow(window);
  }

  /**
   * Register the call backs from GLFW for the keyboard, mouse clicks, and scrolling.
   */
  private void registerCallbacks()
  {
    // Setup a key callback. It will be called every time a key is pressed, repeated or released.
    glfwSetKeyCallback(window, (window, key, scancode, action, mods) ->
    {
        mandelbrot.keyPress(key, action);

      if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
      {
        mandelbrot.clearThreads();
        glfwSetWindowShouldClose(window, true);
      }
    });

    glfwSetMouseButtonCallback(window, (window, button, action, mods) ->
    {
        if (action == GLFW_PRESS)
        {
          mandelbrot.doClick();
        }
    });
  }

  private void loop()
  {
    // This line is critical for LWJGL's interoperation with GLFW's
    // OpenGL context, or any context that is managed externally.
    // LWJGL detects the context that is current in the current thread,
    // creates the GLCapabilities instance and makes the OpenGL
    // bindings available for use.
    GL.createCapabilities();

    // Set the clear/background color
    glClearColor(0.0f, 0.0f, 0.25f, 1.0f); // Was that greenish: .3 .7 .6 .0


    // Run the rendering loop until the user has attempted to close
    // the window or has pressed the ESCAPE key.

    long time = System.nanoTime();
    while (!glfwWindowShouldClose(window))
    {
      time = sleep(time);

      glfwPollEvents();

      update();
      render();
      finishRender();
    }
  }
  private long sleep(long last_execution)
  {
    double maxSleepMS = 1000D / 60;
    double msSinceLastSleep = (System.nanoTime() - last_execution) / 1000000D;
    if (maxSleepMS > msSinceLastSleep)
    {
      try
      {
        Thread.sleep((long) (maxSleepMS - msSinceLastSleep));
      } catch (InterruptedException e)
      {
        System.err.println("Sleep Interrupted!");
        e.printStackTrace();
      }
    }
     return System.nanoTime();
  }

  private void update()
  {
    w = getWindowSize();
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glDepthFunc(GL_LESS);
    glShadeModel(GL_SMOOTH);
  }

  private void render()
  {
    glPointSize(10);
    glLineWidth(5F);
    Display.setColor3(Color.BLUE);
    mandelbrot.display();
  }

  /**
   * Displays the rendered frame, and takes care of double buffering stuff.
   */
  private void finishRender()
  {
    glfwSwapBuffers(window); // swap the color buffers
  }

  /**
   * Closes the window and does other cleanup stuff.
   */
  private void terminate()
  {
    mandelbrot.clearThreads();
    // Free the window callbacks and destroy the window
    glfwFreeCallbacks(window);
    glfwDestroyWindow(window);

    // Terminate GLFW and free the error callback
    glfwTerminate();
    glfwSetErrorCallback(null).free();
  }

  public static void doPointCart(double x, double y)
  {
    glVertex2d(x / (w.w / 2D), y / (w.h / 2D));
  }

  public static void doPointOr(double x, double y)
  {
    doPointCart(x - w.w / 2D, w.h / 2D - y);
  }

  public static void setColor3(Color c)
  {
    glColor3f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f);
  }
  public static Color hsb4ToColor(int hue, int saturation, int brightness, int alpha)
  {
    if (hue < 0)
      hue = hue % 360 + 360;
    if (hue >= 360)
      hue %= 360;
    saturation = normalizeInt(saturation, 0, 100);
    brightness = normalizeInt(brightness, 0, 100);

    saturation = saturation * 255 / 100;
    brightness = brightness * 255 / 100;

    int red = 0, green = 0, blue = 0;

    if (saturation == 0) { red = green = blue = brightness; }
    else
    {
      int t1 = brightness;
      int t2 = (255 - saturation) * brightness / 255;
      int t3 = (t1 - t2) * (hue % 60) / 60;

      if (hue < 60) { red = t1; blue = t2; green = t2 + t3; }
      else if (hue < 120) { green = t1; blue = t2; red = t1 - t3; }
      else if (hue < 180) { green = t1; red = t2; blue = t2 + t3; }
      else if (hue < 240) { blue = t1; red = t2; green = t1 - t3; }
      else if (hue < 300) { blue = t1; green = t2; red = t2 + t3; }
      else if (hue < 360) { red = t1; green = t2; blue = t1 - t3; }
      else { red = 0; green = 0; blue = 0; }
    }
    return new Color(red, green, blue, alpha);
  }
  public static WindowSize getWindowSize()
  {
    try (MemoryStack stack = MemoryStack.stackPush())
    {
      //int* width = malloc(1);
      IntBuffer pWidth = stack.mallocInt(1); // int*
      IntBuffer pHeight = stack.mallocInt(1); // int*

      glfwGetWindowSize(window, pWidth, pHeight);
      return new WindowSize(pWidth.get(), pHeight.get());
    } catch (IllegalArgumentException e)
    {
      return new WindowSize(0, 0);
    }
  }
  public static Point getCursorLocationOrigin(WindowSize w)
  {
    try (MemoryStack stack = MemoryStack.stackPush())
    {
      //double* x = malloc(1);
      DoubleBuffer x = stack.mallocDouble(1); // int*
      DoubleBuffer y = stack.mallocDouble(1); // int*

      glfwGetCursorPos(window, x, y);

      return new Point((int) Math.round(x.get()), (int) Math.round(y.get()));
    } catch (IllegalArgumentException e)
    {
      return null;
    }
  }
  public static int normalizeInt(int value, int min, int max)
  {
    if (value <= min)
      return min;
    if (value >= max)
      return max;
    return value;
  }
  public static class WindowSize
  {
    public final int w;
    public final int h;
    public WindowSize(int width, int height)
    {
      w = width;
      h = height;
    }

    public boolean equals(WindowSize o)
    {
      return o!=null && w==o.w && h==o.h;
    }
  }
}