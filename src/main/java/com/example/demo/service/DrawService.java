package com.example.demo.service;

import com.example.demo.model.draw.Shape;

/**
 * The DrawService class provides methods for drawing various shapes and animations.
 */
public class DrawService {
    public static int SIZE = 140;
    public static double A = 896.0;
    public static double B = 280.0;
    public static int CB = 400;
    public static int WB = 600;
    public static double ROTX = 1024.0;
    public static double ROTY = 512.0;

    /**
     * Draws a triangle animation by gradually changing the position of a circle.
     *
     * @throws InterruptedException if the thread is interrupted while sleeping
     */
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

    /**
     * Interpolates between two values based on a given fraction.
     *
     * @param v1 the start value
     * @param v2 the end value
     * @param d  the fraction
     * @return the interpolated value
     */
    public static int interp(final int v1, final int v2, final double d) {
        final int delta = v2 - v1;
        return (int) (delta * d + v1);
    }

    /**
     * Creates a string representation of a circle.
     *
     * @param x the x-coordinate of the circle's center
     * @param y the y-coordinate of the circle's center
     * @return the string representation of the circle
     */
    public static String circle(int x, int y) {
        String s = String.format("C %d %d %d 512 1 %d %d\n", x, y, SIZE, CB, WB);
        return (s);
    }

    /**
     * Creates a string representation of a circle based on a Shape object.
     *
     * @param shape the Shape object representing the circle
     * @return the string representation of the circle
     */
    public static String circle(Shape shape) {
        // // C X Y R FV FT BC BW\n  -  Circular | spot center: X,Y | radius | falloff FV | falloff type FT, cool brightness BC warm brightness BW
        String s = String.format("C %d %d %d 512 1 %d %d\n", shape.getX(), shape.getY(), shape.getSize(), CB, WB);
        return (s);
    }

    /**
     * Creates a string representation of a rectangle.
     *
     * @param x  the x-coordinate of the top-left corner of the rectangle
     * @param y  the y-coordinate of the top-left corner of the rectangle
     * @param x2 the x-coordinate of the bottom-right corner of the rectangle
     * @param y2 the y-coordinate of the bottom-right corner of the rectangle
     * @param cb the cool brightness value
     * @param wb the warm brightness value
     * @return the string representation of the rectangle
     */
    public static String rect(int x, int y, final int x2, final int y2, final int cb, final int wb) {
        String s = String.format("R %d %d %d %d %d %d\n", x, y, x2, y2, cb, wb);
        return (s);
    }

    /**
     * Creates a string representation of a rectangle based on a Shape object.
     *
     * @param shape the Shape object representing the rectangle
     * @return the string representation of the rectangle
     */
    public static String rect(Shape shape) {
        int length = shape.getSize() / 2;
        String s = String.format("R %d %d %d %d %d %d\n", shape.getX() - length, shape.getY() - length, shape.getX() + length, shape.getY() + length, CB, WB);
        return (s);
    }

    /**
     * Draws an ellipse animation with rotation.
     *
     * @throws InterruptedException if the thread is interrupted while sleeping
     */
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

