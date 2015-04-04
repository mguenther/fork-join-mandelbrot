package com.mgu.experimental.forkjoin.mandelbrot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ForkJoinPool;

public class Mandelbrot extends JFrame {

    private static final long serialVersionUID = 1L;

    public final static String FRAME_TITLE = "Mandelbrot with fork/join";

    public final static Dimension CANVAS_SIZE = new Dimension(1024, 768);

    private final MandelbrotCanvas canvas;

    private JLabel status;

    public Mandelbrot() {
        super(Mandelbrot.FRAME_TITLE);

        this.canvas = new MandelbrotCanvas();
        this.canvas.setSize(Mandelbrot.CANVAS_SIZE);
        this.canvas.setPreferredSize(Mandelbrot.CANVAS_SIZE);
        this.canvas.setMaximumSize(Mandelbrot.CANVAS_SIZE);
        this.canvas.setMinimumSize(Mandelbrot.CANVAS_SIZE);

        this.add(this.canvas, BorderLayout.CENTER);
        this.add(createButtonPanel(), BorderLayout.SOUTH);

        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setVisible(true);
    }

    private JPanel createButtonPanel() {
        JButton runButton = new JButton("run");
        runButton.addActionListener(new RunAction());

        JButton exitButton = new JButton("exit");
        exitButton.addActionListener(new ExitApplicationAction());

        this.status = new JLabel("Execution time: <No result>");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(status);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(runButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(exitButton);

        return buttonPanel;
    }

    class RunAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            canvas.clearImage();

            final int[] imageData = new int[CANVAS_SIZE.width * CANVAS_SIZE.height];

            MandelbrotAction proc = new MandelbrotAction(
                    imageData,
                    0,
                    CANVAS_SIZE.height);

            ForkJoinPool forkJoinPool = new ForkJoinPool(4);
            long start = System.currentTimeMillis();
            forkJoinPool.invoke(proc);

            canvas.updateImage(imageData);
            long end = System.currentTimeMillis();

            long time = end - start;
            status.setText("Execution time: " + time + " ms");
        }
    }

    class ExitApplicationAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            dispose();
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        new Mandelbrot();
    }
}