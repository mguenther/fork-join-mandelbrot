package com.mgu.experimental.forkjoin.mandelbrot;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;

public class ColorUtils {
    public static ColorModel generateColorModel() {
        byte[] r = new byte[255];
        byte[] g = new byte[255];
        byte[] b = new byte[255];

        for (int i = 0; i < 255; i++) {
            r[i] = (byte) ((i * 26) % 250);
            g[i] = (byte) ((i * 2) % 250);
            b[i] = (byte) ((i * 35) % 250);
        }

        return new IndexColorModel(8, 255, r, g, b);
    }
}