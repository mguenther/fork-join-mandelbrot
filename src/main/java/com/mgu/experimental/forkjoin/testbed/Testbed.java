package com.mgu.experimental.forkjoin.testbed;

abstract public class Testbed {

    abstract public void simulate();

    protected double mean(long[] values) {
        double sum = 0.0;
        for (long value : values) {
            sum += value;
        }
        return sum / values.length;
    }

    protected double stddev(long[] values) {
        final double mean = mean(values);
        double stddevTotal = 0.0;
        for (long value : values) {
            double dev = value - mean;
            stddevTotal += dev * dev;
        }
        return Math.sqrt(stddevTotal / values.length);
    }

    protected void collectGarbage() {
        for (int i = 0; i < 3; i++) {
            System.gc();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}