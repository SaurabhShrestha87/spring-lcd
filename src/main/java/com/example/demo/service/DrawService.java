package com.example.demo.service;

import com.example.demo.model.draw.Shape;

public class DrawService {
    public static int SIZE = 140;
    public static double A = 896.0;
    public static double B = 280.0;
    public static int CB = 400;
    public static int WB = 600;
    public static double ROTX = 1024.0;
    public static double ROTY = 512.0;


    public static void triangle() throws InterruptedException {
        for (double d = 0.0; d < 1.0; d += 0.03) {
            final int x = interp(128, 1920, d);
            circle(x, 128);
            Thread.sleep(30L);
        }
        for (double d = 0.0; d < 1.0; d += 0.03) {
            final int x = interp(1920, 1024, d);
            final int y = interp(128, 1920, d);
            circle(x, y);
            Thread.sleep(30L);
        }
        for (double d = 0.0; d < 1.0; d += 0.03) {
            final int x = interp(1024, 128, d);
            final int y = interp(1920, 128, d);
            circle(x, y);
            Thread.sleep(30L);
        }
    }

    public static int interp(final int v1, final int v2, final double d) {
        final int delta = v2 - v1;
        return (int) (delta * d + v1);
    }

    public static String circle(int x, int y) {
        String s = String.format("C %d %d %d 512 1 %d %d\n", x, y, SIZE, CB, WB);
        return (s);
    }

    public static String rect(int x, int y, final int x2, final int y2, final int cb, final int wb) {
        String s = String.format("R %d %d %d %d %d %d\n", x, y, x2, y2, cb, wb);
        return (s);
    }

    public static String rect(Shape shape, final int cb, final int wb) {
        int length = shape.getSize() / 2;
        String s = String.format("R %d %d %d %d %d %d\n", shape.getX() - length, shape.getY() - length, shape.getX() + length, shape.getY() + length, cb, wb);
        return (s);
    }

    public void ellipseRotated() throws InterruptedException {
        for (double angle = 0.0; angle < 9.42477796076938; angle += 0.04) {
            int b = 384;
            if (angle > 3.141592653589793) {
                b = 256;
            }
            int x = (int) (A * Math.cos(angle)) + 1024;
            int y = (int) (b * Math.sin(angle)) + 512;
            final double dx = x - ROTX;
            final double dy = y - ROTY;
            final double radius = Math.sqrt(dx * dx + dy * dy);
            double nAngle = Math.atan2(dy, dx);
            if (angle > 3.141592653589793) {
                nAngle += (angle - 3.141592653589793) / 2.0;
            }
            x = (int) (Math.cos(nAngle) * radius);
            x += (int) ROTX;
            y = (int) (Math.sin(nAngle) * radius);
            y += (int) ROTY;
            if (angle > 3.141592653589793 && angle < 6.283185307179586) {
                y += (int) ((angle - 3.141592653589793) / 3.141592653589793 * 512.0);
            }
            if (angle > 6.283185307179586) {
                y += (int) (512.0 - (angle - 6.283185307179586) / 3.141592653589793 * 512.0);
            }
            circle(x, y);
            Thread.sleep(2L);
        }
    }
}

