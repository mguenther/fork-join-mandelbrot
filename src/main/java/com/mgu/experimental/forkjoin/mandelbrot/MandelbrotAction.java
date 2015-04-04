package com.mgu.experimental.forkjoin.mandelbrot;

import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class MandelbrotAction extends RecursiveAction {

    private static final long serialVersionUID = 1028828389300743580L;

    private final Invoker invoker = new SafeInvoker();

    private final int maxJobSize;

    private int[] imageData;

    private final int from;

    private final int to;

    public MandelbrotAction(
            int[] imageData,
            final int from,
            final int to) {
        this(imageData, from, to, 32);
    }

    public MandelbrotAction(
            int[] imageData,
            final int from,
            final int to,
            final int maxJobSize) {
        this.imageData = imageData;
        this.from = from;
        this.to = to;
        this.maxJobSize = maxJobSize;
    }

    @Override
    protected void compute() {
        final int size = this.to - this.from;

        if (size <= maxJobSize) {
            computeDirectly();
        } else {
            parallelize();
        }
    }

    private void computeDirectly() {
        final int width = Mandelbrot.CANVAS_SIZE.width;
        final int height = Mandelbrot.CANVAS_SIZE.height;

        final double xReal = -2.0; //-2.1;
        final double xImag = 1.25; //1.1;

        final double yReal = 1.2; //-1.25;
        final double yImag = -1.25; //1.25;

        for (int i = this.from; i < this.to; i++) {
            for (int j = 0; j < width; j++) {
                int imageIndex = (width * i + j);

                this.imageData[imageIndex] = MandelbrotCalculator.iterate(
                        xReal + (xImag - xReal) * j / (width - 1),
                        yReal + (yImag - yReal) * i / (height - 1)) % 256;
            }
        }
    }

    private void parallelize() {
        final int half = (this.to - this.from) >> 1;

        MandelbrotAction left = new MandelbrotAction(
                this.imageData,
                this.from,
                this.from + half,
                this.maxJobSize);

        MandelbrotAction right = new MandelbrotAction(
                this.imageData,
                this.from + half,
                this.to,
                this.maxJobSize);

        this.invoker.invoke(left, right);
    }

    private interface Invoker {
        <V> void invoke(ForkJoinTask<V> left, ForkJoinTask<V> right);
    }

    class SafeInvoker implements Invoker {
        @Override
        public <V> void invoke(ForkJoinTask<V> left, ForkJoinTask<V> right) {
            invokeAll(left, right);
        }
    }

    class CorrectManualInvoker implements Invoker {
        @Override
        public <V> void invoke(ForkJoinTask<V> left, ForkJoinTask<V> right) {
            right.fork();
            left.invoke();
            right.join();
        }
    }

    class SequentialPreOrderInvoker implements Invoker {
        @Override
        public <V> void invoke(ForkJoinTask<V> left, ForkJoinTask<V> right) {
            left.fork();
            left.join();
            right.invoke();
        }
    }

    class SequentialPostOrderInvoker implements Invoker {
        @Override
        public <V> void invoke(ForkJoinTask<V> left, ForkJoinTask<V> right) {
            right.invoke();
            left.fork();
            left.join();
        }
    }
}