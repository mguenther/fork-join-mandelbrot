package com.mgu.experimental.forkjoin.mandelbrot;

public class MandelbrotCalculator {

    private final static int MAX_ITERATIONS = 1000;

    public static int iterate(double x, double y) {
        int iter = 0;

        double aold = 0;
        double bold = 0;
        double a = 0;
        double b = 0;
        double asquared = a * a;
        double bsquared = b * b;

        a = x;
        b = y;

        double zsquared = asquared + bsquared;

        for (iter = 0; iter < MAX_ITERATIONS; iter++) {
            a = asquared - bsquared + x;

            asquared = a * a;

            b = 2 * aold * b + y;

            if (bold == b && aold == a) {
                iter = MAX_ITERATIONS - 1;
            }

            bsquared = b * b;

            zsquared = asquared + bsquared;

            if (zsquared > 4) {
                break;
            }

            bold = b;
            aold = a;
        }

        return iter;
    }
}