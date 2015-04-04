[![Build Status](https://travis-ci.org/mguenther/fork-join-mandelbrot.svg?branch=master)](https://travis-ci.org/mguenther/fork-join-mandelbrot.svg)

# Calculating Mandelbrot sets using Fork/Join (Java 7)

This project shows how to use the Fork/Join framework that was introduced with Java 7 to parallelize CPU-bound tasks. The implementation comprises two parts: A Swing-based application which calculates (parallel part of the application) and visualizes (sequential part of the application) the Mandelbrot set, and a testbed which runs the Mandelbrot calculation with different configurations in order to investigate the impact of different parameterizations of workload and number of threads on execution time. Testbed results are visualized with Gnuplot scripts.

## Run the Swing application ##

The code for the Swing application is located at package `com.mgu.experimental.forkjoin.mandelbrot`. Class `Mandelbrot` provides a `main` method which launches the application. Workload and number of fork-join threads are hard-coded. If you want to tinker around with different settings, please adjust the following the lines of code to your needs.

### Adjusting workload size ###

Lines 70-73 in `com.mgu.experimental.forkjoin.mandelbrot.Mandelbrot`:
```
MandelbrotAction proc = new MandelbrotAction(
    imageData,
    0,
    CANVAS_SIZE.height);
```
`CANVAS_SIZE.height` is used to determine the workload size in the Swing application.

### Adjusting the number of threads ###

Line 75 in `com.mgu.experimental.forkjoin.mandelbrot.Mandelbrot`:
```
ForkJoinPool forkJoinPool = new ForkJoinPool(4);
```
`4` determines the number of fork-join threads associated with the `ForkJoinPool`.

### Using different invocation strategies ###

Forking and joining tasks is by no means trivial and can lead to subtle errors if done wrong. The API of the fork/join provides an `invokeAll` method, which should be used to let the framework handle forking and joining. However, it does not prevent the manual usage of methods `fork`, `invoke` and `join`. Using these methods in the wrong order will lead to a sequential execution of your parallel code. `MandelbrotAction` shows the different approaches when forking and joining.

```
class SafeInvoker implements Invoker {
    @Override
    public <V> void invoke(ForkJoinTask<V> left, ForkJoinTask<V> right) {
        invokeAll(left, right);
    }
}
```

`SafeInvoker` uses `invokeAll` and thus lets the framework decide how to fork, invoke and join the individual tasks. I recommend to use this method at all times.

```
class CorrectManualInvoker implements Invoker {
    @Override
    public <V> void invoke(ForkJoinTask<V> left, ForkJoinTask<V> right) {
        right.fork();
        left.invoke();
        right.join();
    }
}
```

`CorrectManualInvoker` shows the correct manual usage. Fork the second task first, then invoke the execution of the first task within the execution context of the current Thread (might get forked again), and call `join` on the second task to wait for its completion.

The pathological cases are:

```
class SequentialPreOrderInvoker implements Invoker {
    @Override
    public <V> void invoke(ForkJoinTask<V> left, ForkJoinTask<V> right) {
        left.fork();
        left.join();
        right.invoke();
    }
}
```

This will compute the results of all `left` tasks before invoking any execution of `right` and thus lead to a deformed fork/join execution tree which has the form of a linear list. Thus, using this invocation strategy will execute a sequential computation process on your parallel code.

```
class SequentialPostOrderInvoker implements Invoker {
    @Override
    public <V> void invoke(ForkJoinTask<V> left, ForkJoinTask<V> right) {
        right.invoke();
        left.fork();
        left.join();
    }
}
```

Similar to the aforementioned invocation strategy, this one will execute all `right` tasks before any `left` task. This also yields a sequential computation process.

## Run the testbed ##

The code for the testbed is located at package `com.mgu.experimental.forkjoin.testbed`. `MandelbrotTestbed` utilizes `MandelbrotAction` of the Swing application to execute the Mandelbrot computation with different configurations. `MandelbrotTestbed` provides a main method which launches the testbed. Testbed parameters are again hardcoded. Adjust `REPETITIONS` to to set the number of repeated runs to your liking. Parameters for number of threads (pool size) and workload size range from:

* Number of threads: 1, 1, 2, 4, 8, 16, 32, 64, 128 (the first parameter is repeated to warm-up the JVM)
* Workload sizes: 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048

Please note that the integer array which olds the result of the Mandelbrot computation is larger than 512 KB and would be collected by the GC during the next full GC cycle. Furthermore, the Hotspot compiler could inline methods if it detects a hot spot. It is recommended to set the JVM flag `-XX:CompileThreshold=1` to trigger method inlining as soon as possible (this is the reason why the first run (cf. number of threads) is always repeated. The testbed triggers a GC run after every individual run and gives the GC some time to perform its action. However, this is just a heuristic! It may not prevent GC runs during the actual execution which in turn can affect the results and introduce outliers in your results. Thus, it is best to even out outliers by repeating the experiments multiple times (cf. parameter `REPETITIONS`).

Testbed results are printed directly to stdout. Save the results to a file if you want to visualize them using the provided Gnuplot scripts.

## Visualizing testbed results ##

Gnuplot scripts for printing the results of the parameterization test and visualizing the speedup factor when utilizing more threads are located at folder `src/main/gnuplot`. Both scripts will look for your test data at `src/main/resources`. Please consult the scripts for details on file names. There are script variants for EPS and PNG output.

Invoking the scripts with `gnuplot <GNUPLOT_FILE>` will yield the visualization in the resp. image format at the folder the scripts were invoked from.

## Testbed run on Intel i7-2700 with 4 cores at 3.50 GHz and HT ##

Back in 2012 when the fork/join framework was a fresh and hot topic in the Java world, a colleague of mine and myself did an in-depth discussion of the inner workings of the fork/join framework along with a detailed analysis on the impact of different parameterizations on the execution time for the German Java magazine JavaSPEKTRUM (published with issue 05/2012). You can grab the article [![here](http://www.accso.de/images/stories/accso/dokumente/2012_guenther-lehmann-javaspektrum-09-12.pdf)](http://www.accso.de/images/stories/accso/dokumente/2012_guenther-lehmann-javaspektrum-09-12.pdf) (German only).

We ran the Mandelbrot calculation several times on a Intel i7-2700 with 4 cores at 3.50 GHz (HT). The results are shown underneath.

[![Optimal Parameterization](https://dl.dropboxusercontent.com/u/8084425/optimal_parameterization.png)](https://dl.dropboxusercontent.com/u/8084425/optimal_parameterization.png)

* The results indicate that the optimal parameterization is somewhere between 8 and 16 fork/join threads and and uses a workload configuration of 32.
* As expected, edge cases exhibit an execution time that is close to a sequential execution.
* Execution times can be steadily decreased as we go up the 32 fork/join threads. Past this point, the thread contention is too high and, thus decreasing overall throughput which leads to an increased execution time.
* If you look at the standard deviation (not shown in the image), you can also see that results have a higher deviation (18-20 ms as opposed to 1-2 ms) with thread numbers past 64, which also indicates a higher thread contention.

## License ##

This work is released under the terms of the MIT license.
