package com.mgu.experimental.forkjoin.mandelbrot;

import javax.swing.*;
import java.awt.*;
import java.awt.image.MemoryImageSource;

public class MandelbrotCanvas extends JComponent {

    private static final long serialVersionUID = 5877387869259467541L;

    private Image image;

    @Override
    protected void paintComponent(Graphics g) {
        Rectangle r = getBounds();

        g.setColor(Color.WHITE);
        g.fillRect(r.x, r.y, r.width, r.height);

        if (this.image != null) {
            g.drawImage(this.image, 0, 0, null);
        }
    }

    public void clearImage() {
        this.image = null;
        repaint();
    }

    public void updateImage(int[] imageData) {
        final MemoryImageSource mis = new MemoryImageSource(
                Mandelbrot.CANVAS_SIZE.width,
                Mandelbrot.CANVAS_SIZE.height,
                ColorUtils.generateColorModel(),
                imageData,
                0,
                Mandelbrot.CANVAS_SIZE.width);
        Image image = this.getToolkit().createImage(mis);

        if (this.image != null) {
            this.image.flush();
        }

        this.image = image;

        repaint();
    }
}