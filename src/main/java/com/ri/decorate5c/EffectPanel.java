package com.ri.decorate5c;

import java.awt.*;
import java.awt.geom.Path2D;

import javax.swing.JPanel;

class EffectPanel extends JPanel {
    private static final Stroke stroke5  = new BasicStroke(Decorate5c.w / 108f);
    private static final Stroke stroke9  = new BasicStroke(Decorate5c.w / 60f);
    private static final Stroke stroke13 = new BasicStroke(Decorate5c.w / 40f);

    static final float[]      heartCoordinates = new float[]{0f, -0.5f, 0.2f, -0.65f, 0.4f, -0.65f, 0.6f, -0.5f, 0.675f, -0.3f, 0.6f, -0.1f, 0.4f, 0.1f, 0.2f, 0.3f, 0.0f, 0.5f, -0.2f, 0.3f, -0.4f, 0.1f, -0.6f, -0.1f, -0.675f, -0.3f, -0.6f, -0.5f, -0.4f, -0.65f, -0.2f, -0.65f};
    static final Path2D.Float heartShape       = new Path2D.Float();

    static {
        float x = Float.NaN;
        float y = Float.NaN;
        for (int i = 2; i < heartCoordinates.length + 4; i += 2) {
            float scale = Decorate5c.w / 9f;
            float preX = heartCoordinates[(i - 2)%heartCoordinates.length]*scale;
            float preY = heartCoordinates[(i - 1)%heartCoordinates.length]*scale;
            float crrX = heartCoordinates[(i)%heartCoordinates.length]*scale;
            float crrY = heartCoordinates[(i + 1)%heartCoordinates.length]*scale;
            float nxtX = heartCoordinates[(i + 2)%heartCoordinates.length]*scale;
            float nxtY = heartCoordinates[(i + 3)%heartCoordinates.length]*scale;

            float nxtMpreX = (nxtX - preX);
            float nxtMpreY = (nxtY - preY);

            float angle = (float) Math.abs(Math.atan2(preY - crrY, preX - crrX) - Math.atan2(nxtY - crrY, nxtX - crrX));

            float x0, y0;
            if (angle < 2.1) {
                x0 = 0;
                y0 = 0;
            } else {
                x0 = nxtMpreX*0.2f;
                y0 = nxtMpreY*0.2f;
            }

            float x1 = crrX - x0;
            float y1 = crrY - y0;

            if (Float.isFinite(x)) {
                heartShape.curveTo(x, y, x1, y1, crrX, crrY);
            } else {
                heartShape.moveTo(crrX, crrY);
            }

            x = crrX + x0;
            y = crrY + y0;
        }
    }

    private final Decorate5c decorate5c;

    public EffectPanel(final Decorate5c decorate5c) {this.decorate5c = decorate5c;}

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);
        if (decorate5c.effects.isEmpty() || decorate5c.pause) return;

        Graphics2D g = (Graphics2D) graphics;

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        long time = System.currentTimeMillis();
        for (Effect effect: decorate5c.effects) {
            float t = (float) (time - effect.startTime)/effect.duration;
            if (t > 1.0) continue;
            double s = Math.pow(t, 0.5);

            int   alpha        = (int) ((1f - t*t*t)*255);
            int   shadowAlpha1 = (int) ((1f - t*t)*128);
            int   shadowAlpha2 = (int) ((1f - t)*64);
            Color baseColor    = new Color(alpha << 24 | (effect.color.getRGB() & 0xFFFFFF), true);
            Color shadowColor1 = new Color(shadowAlpha1 << 24 | (effect.color.getRGB() & 0xFFFFFF), true);
            Color shadowColor2 = new Color(shadowAlpha2 << 24 | (effect.color.getRGB() & 0xFFFFFF), true);

            // for circle
            // float r = ((t)*50 + 4) / 64f;
            // ((Graphics2D) g).setStroke(stroke13);
            // g.setColor(shadowColor2);
            // g.drawOval(effect.position.x - r, effect.position.y - r, 2*r, 2*r);
            //
            // ((Graphics2D) g).setStroke(stroke9);
            // g.setColor(shadowColor1);
            // g.drawOval(effect.position.x - r, effect.position.y - r, 2*r, 2*r);
            //
            // ((Graphics2D) g).setStroke(stroke5);
            // g.setColor(baseColor);
            // g.drawOval(effect.position.x - r, effect.position.y - r, 2*r, 2*r);

            g.translate(effect.position.x, effect.position.y);
            g.scale(s, s);

            g.setStroke(stroke13);
            g.setColor(shadowColor2);
            g.draw(heartShape);

            g.setStroke(stroke9);
            g.setColor(shadowColor1);
            g.draw(heartShape);

            g.setStroke(stroke5);
            g.setColor(baseColor);
            g.draw(heartShape);

            g.scale(1d/s, 1d/s);
            g.translate(-effect.position.x, -effect.position.y);
        }
    }

    record Effect(Point position, Color color, long startTime, int duration) {}
}
