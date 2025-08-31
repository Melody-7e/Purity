package com.ri.helper;

import static java.lang.Math.sqrt;

// @formatter:off
public class PurityMaths {
    public static final float SQRT_2 = (float) sqrt(2.0);
    public static final float SQRT_3 = (float) sqrt(3.0);
    public static final float SQRT_5 = (float) sqrt(5.0);
    public static final float PHI    = (SQRT_5 + 1) / 2;
    public static final float PEI    = (float) (1 / (Math.PI * Math.E));

    public static void roundToHex(double x, double y, double a, double[] out) {
        double u = x / a;
        double v = y / a;

        double ax = positiveMod(u,          2.0)            - 1.0;
        double ay = positiveMod(v,          2.0 * SQRT_3)   - SQRT_3;
        double bx = positiveMod(u - 1.0,    2.0)            - 1.0;
        double by = positiveMod(v - SQRT_3, 2.0 * SQRT_3)   - SQRT_3;

        double da = hexDist(ax, ay);
        double db = hexDist(bx, by);

        double x_ = u - ((da < db) ? ax : bx);
        double y_ = v - ((da < db) ? ay : by);

        out[0] = x_ * a;
        out[1] = y_ * a;
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }


    public static float positiveMod(float a, float b) {
        float r = a % b;
        if (r < 0) return r + b;
        return r;
    }

    public static double positiveMod(double a, double b) {
        double r = a % b;
        if (r < 0) return r + b;
        return r;
    }

    public static double hexDist(double x, double y) {
        double ax = Math.abs(x);
        double ay = Math.abs(y);

        return Math.max((ax + ay * SQRT_3) * 0.5f, ax);
    }

    public static int linearToSrgb(float linearComponent) {
        if (linearComponent > 1) return 0xFF;
        if (linearComponent < 0) return 0;

        float normalizedSrgb;
        if (linearComponent <= 0.0031308f) {
            normalizedSrgb = linearComponent * 12.92f;
        } else {
            normalizedSrgb = (float) (1.055f * Math.pow(linearComponent, 1.0f / 2.4f) - 0.055f);
        }
        // Convert back to 0-255 range and round
        return Math.round(normalizedSrgb * 255.0f);
    }

    public static float srgbToLinear(int srgbComponent) {
        float normalizedSrgb = srgbComponent / 255.0f;

        if (normalizedSrgb <= 0.04045f) {
            return normalizedSrgb / 12.92f;
        } else {
            return (float) Math.pow((normalizedSrgb + 0.055f) / 1.055f, 2.4f);
        }
    }

    public static int log2(int x) {
        int r = 0;

        if ((x & 0xffff_0000) != 0) {
            r += 5;
            x >>= 5;
        }

        if ((x & 0xff_00) != 0) {
            r += 4;
            x >>= 4;
        }

        if ((x & 0xf_0) != 0) {
            r += 3;
            x >>= 3;
        }

        if ((x & 0b11_00) != 0) {
            r += 2;
            x >>= 2;
        }

        if ((x & 0b1_0) != 0) {
            r += 1;
        }

        return r;
    }

    public static double soundNoteCurve(double x) {
        return Math.pow(x, 0.25) * Math.pow(1 - x, 1.618033) * 2;
    }
}
// @formatter:on
