# Java Mandelbrot Renderer

_By Stanley S, written in 2018-2019._

### Project History

This was a personal project of mine in high school. I be came very fascinated by fractals, especially the Mandelbrot Set, and found that it was not too difficult to calculate and generate. At the time I had recently discovered the LWJGL (LightWeight Java Gaming Library, what Minecraft uses) and how it could draw things to a window.

### The Math

The [Mandelbrot Set](https://en.wikipedia.org/wiki/Mandelbrot_set) is a 2D fractal in the [complex numbers](https://en.wikipedia.org/wiki/Complex_number) that comes from the limiting behavior of iterating a function: $z_{n+1} = z_n^2 + c$ where $z_0 = 0$ and $c$ is the starting coordinate of the point. The point is said to be inside the set if as $n$ goes to infinity, $z_n$ stays finite, and outside if the magnitude of $z_n$ explodes to infinity. While drawing the points that are "inside" and "outside" results in a chaotic region, two colors is not very interesting.

When calculating the sequence of values, if $z_n$'s magnitude ever increases above 2, it will always explode to infinity, so we can stop computing at that point. In fact we can use the iteration number, $n$ to know how long it took to get there to get a relatively good estimate on how long it took to explode to infinity, and use that to color the pixel based on that number of steps. That is what this program does.

A related fractal, the Julia Set, does something similar, but instead of $c$ changing for each pixel, $z_0$ varies for each location, and there is a set value $c$ for the whole image. This generates a new and rotationally symmetric fractal.

### Screenshots

(coming soon)

### Running
Download the repository and run the following:  
  Mac or *nix: `./gradlew build` then `./gradlew run`  
  or on Windows: `.\gradlew.bat build` and `.\gradlew.bat run`

This should open a window that will begin slowly drawing the fractal.

### Controls:

clicking - Re-centers the view on the clicked location. (Clears the cache)  
`+` or `=` - Zoom In (If the image is done, it will be cached)  
`-` or `_` - Zoom Out (If the image is done, it will be cached)  
`s` - **__S__**ave the completed image to the `img` directory  
`w` - quick-**__W__**rite current view to disk so it can be loaded later  
`r` - quick-**__R__**ead a past view from disk to display again  
`l` - **__L__**oop: will zoom in after the frame finishes loading. If `s` was the last key pressed, this will save each frame to the `img` directory.  
`z` - **__Z__**oom: Resets the zoom to zoomed all the way out, or zoomed nearly to the limit.  
`v` - **__V__**iewframe  
`j` - switch between **__J__**ulia and Mandelbrot fractals.  
