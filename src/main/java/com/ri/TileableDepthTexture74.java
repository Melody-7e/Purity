package com.ri;

import com.ri.helper.PurityMaths;
import com.ri.meta.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.imageio.ImageIO;

public class TileableDepthTexture74 {
    public static final int seed = 0;

    public static final int w = 1024;
    public static final int h = 1024;

    public static final int rMin = 16;
    public static final int rMax = 72;

    public static final int iterations = 3072;

    public static final float chroma = 0.015f;
    public static final float minLum = 0.09f;
    public static final float maxLum = 0.95f;


    @SuppressWarnings("UnnecessaryLocalVariable")
    public static void main(String[] args) throws Exception {
        // @formatter:off
        String          _clazzName  = TileableDepthTexture74.class.getSimpleName();
        ProjectType     type        = ProjectType.EASYEDIT_IMAGE;
        ProjectPD       pd          = ProjectPD.LEFT;
        ProjectCategory category    = ProjectCategory.USABLE;
        byte            id          = (byte) Integer.parseInt(_clazzName.substring(_clazzName.length() - 2), 16);
        String          name        = _clazzName;
        ProjectState    state       = ProjectState.OKAY;
        // @formatter:on

        ProjectName projectName = new ProjectName(type, pd, category, id, name, state);
        Projects.getInstance().checkName(projectName);

        System.out.print("================================ ");
        System.out.println(projectName.getFullName());

        execute(projectName);

        System.out.println();
        System.out.println("SUCCESS");
    }

    private static void execute(ProjectName projectName) throws Exception {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(PurityMaths.oklchToSrgb(minLum, 0.0, 0.0)));
        g.fillRect(0, 0, w, h);

        Random rand = new Random(seed);
        for (int i = 0; i < iterations; i++) {
            int x = rand.nextInt(0, w);
            int y = rand.nextInt(0, h);
            int r = rand.nextInt(rMin, rMax);

            float l = (float) i / iterations;
            g.setColor(new Color(PurityMaths.oklchToSrgb(l * (maxLum - minLum) + minLum, chroma, rand.nextFloat(360))));

            fillOvalTiled(g, x - r / 2, y - r / 2, r, r);
        }
        g.dispose();

        ImageIO.write(img, "png", projectName.getFile(".png"));
    }

    private static void fillOvalTiled(Graphics2D g, int x, int y, int w0, int h0) {
        g.fillOval(x, y, w0, h0);

        boolean wraps_left = (x < 0);
        boolean wraps_right = (x + w0 > w);
        boolean wraps_top = (y < 0);
        boolean wraps_bottom = (y + h0 > h);

        if (wraps_left) {
            g.fillOval(x + w, y, w0, h0);
        }
        if (wraps_right) {
            g.fillOval(x - w, y, w0, h0);
        }

        if (wraps_top) {
            g.fillOval(x, y + h, w0, h0);
        }
        if (wraps_bottom) {
            g.fillOval(x, y - h, w0, h0);
        }

        if (wraps_left && wraps_top) {
            g.fillOval(x + w, y + h, w0, h0);
        }
        if (wraps_right && wraps_top) {
            g.fillOval(x - w, y + h, w0, h0);
        }
        if (wraps_left && wraps_bottom) {
            g.fillOval(x + w, y - h, w0, h0);
        }
        if (wraps_right && wraps_bottom) {
            g.fillOval(x - w, y - h, w0, h0);
        }
    }
}
