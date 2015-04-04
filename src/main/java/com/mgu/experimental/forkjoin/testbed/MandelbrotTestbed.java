package com.mgu.experimental.forkjoin.testbed;

import com.mgu.experimental.forkjoin.mandelbrot.MandelbrotAction;

import java.util.concurrent.ForkJoinPool;

public class MandelbrotTestbed extends Testbed {

    private final int REPETITIONS = 10;

    private long measure(int forkJoinPoolSize, int maximumJobSize, int imageWidth, int imageHeight) {
        final int[] imageData = new int[imageWidth*imageHeight];

        MandelbrotAction proc = new MandelbrotAction(imageData, 0, imageHeight);
        ForkJoinPool forkJoinPool = new ForkJoinPool(forkJoinPoolSize);
        long start = System.currentTimeMillis();
        forkJoinPool.invoke(proc);
        long end = System.currentTimeMillis();
        long time = end - start;

        return time;
    }

    @Override
    public void simulate() {
        final int[] poolSizes = new int[] {1, 1, 2, 4, 8, 16, 32, 64, 128 };
        for (int i = 0; i < poolSizes.length; i++) {
            int maximumJobSize = 1;
            while (maximumJobSize <= 2048) {
                final long[] times = new long[REPETITIONS];
                for (int j = 0; j < REPETITIONS; j++) {
                    times[j] = measure(poolSizes[i], maximumJobSize, 2048, 1536);
                    collectGarbage();
                }
                final double mean = mean(times);
                final double stddev = stddev(times);
                System.out.println(poolSizes[i] + "\t" + maximumJobSize + "\t" + mean + "\t" + stddev);
                maximumJobSize = maximumJobSize << 1;
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        new MandelbrotTestbed().simulate();
    }
}