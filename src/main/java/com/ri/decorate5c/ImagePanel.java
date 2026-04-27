package com.ri.decorate5c;

import com.ri.helper.PurityMaths;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;

import javax.swing.JPanel;

class ImagePanel extends JPanel {
    private final Decorate5c decorate5c;

    public ImagePanel(final Decorate5c decorate5c) {
        this.decorate5c = decorate5c;
    }

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);
        if (decorate5c.image == null || decorate5c.pause) return;

        Graphics2D g = (Graphics2D) graphics;

        float t = (float) (System.currentTimeMillis() - decorate5c.pImgTime)/Decorate5c.TRANSITION_DURATION;

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (t > 1.0 || t < 0.0 || decorate5c.pImage == null) {
            drawImage(g, decorate5c.image, decorate5c.imgScale, decorate5c.imgX, decorate5c.imgY, true);
        } else {
            drawImage(g, decorate5c.image, decorate5c.imgScale*(1 + (1 - t)*(1 - t)*0.3f), decorate5c.imgX, decorate5c.imgY, true);

            t = Math.min(t*3, 1);

            float alpha = (float) Math.pow(1 - t, 0.8);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            drawImage(g, decorate5c.pImage, decorate5c.pImgScale*(1 + t*t*0.2f), decorate5c.pImgX, decorate5c.pImgY, false);

            repaint();
        }

        float overlayAlpha = 1.0f - decorate5c.brightness;
        if (overlayAlpha > PurityMaths.EPSILON) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, overlayAlpha));
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, Decorate5c.w, Decorate5c.h);
        }
    }

    public BufferedImage applyTempTint(BufferedImage src, float temp, float tint) {
        byte[] r = new byte[256];
        byte[] g = new byte[256];
        byte[] b = new byte[256];

        for (int i = 0; i < 256; i++) {
            float redShift   = i + (temp*36f);
            float greenShift = i - (tint*72f);
            float blueShift  = i - (temp*36f);

            r[i] = (byte) Math.max(0, Math.min(255, (int) redShift));
            g[i] = (byte) Math.max(0, Math.min(255, (int) greenShift));
            b[i] = (byte) Math.max(0, Math.min(255, (int) blueShift));
        }

        ByteLookupTable table = new ByteLookupTable(0, new byte[][]{r, g, b});
        LookupOp        op    = new LookupOp(table, null);

        Raster         srcRaster = src.getRaster();
        WritableRaster dstRaster = srcRaster.createCompatibleWritableRaster();
        op.filter(srcRaster, dstRaster);

        return new BufferedImage(src.getColorModel(), dstRaster, src.isAlphaPremultiplied(), null);
    }

    private void drawImage(Graphics2D g, BufferedImage image, float scale, float imgX, float imgY, boolean overwrite) {
        scale = Math.max(Math.nextUp(1.0f), scale);

        float imgW = image.getWidth();
        float imgH = image.getHeight();

        float drawW, drawH;
        if (imgW/imgH > (float) Decorate5c.w/Decorate5c.h) {
            drawW = ((scale)*Decorate5c.h*imgW/imgH);
            drawH = ((scale)*Decorate5c.h);
        } else {
            drawW = ((scale)*Decorate5c.w);
            drawH = ((scale)*Decorate5c.w*imgH/imgW);
        }

        imgX = Math.clamp(imgX, (Decorate5c.w - drawW)/2, -(Decorate5c.w - drawW)/2);
        imgY = Math.clamp(imgY, (Decorate5c.h - drawH)*2/3, -(Decorate5c.h - drawH)/3);

        if (overwrite) {
            decorate5c.imgX = (int) imgX;
            decorate5c.imgY = (int) imgY;
        }

        float x = ((Decorate5c.w - drawW)/2 + imgX);
        float y = ((Decorate5c.h - drawH)/3 + imgY);

        AffineTransform tx = new AffineTransform();
        tx.translate(x, y);
        tx.scale(drawW/imgW, drawH/imgH);
        g.drawImage(applyTempTint(image, decorate5c.temperature, decorate5c.tint), tx, null);
    }
}
