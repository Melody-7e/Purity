package com.ri.decorate5c;

import static com.ri.decorate5c.EffectPanel.heartCoordinates;

import com.ri.helper.PurityMaths;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;


/**
 * <h1>Info:</h1> It fetches images from <a href="https://pinterest.com">pinterest</a> and renders it on a window and refresh
 * that image every some time, that all.
 * <br><br>
 * <h2>Commands:</h2>
 * <ul>
 *     <li><b>Click</b>: Draws heart/ or circle</li>
 *     <li><b>Drag</b>: Draws a line of hearts</li>
 *     <li></li>
 *     <li><b>Ctrl+Click</b>: Select the image
 *     (like you clicked on the image on pinterest so it recommends more similar images)
 *     </li>
 *     <li><b>Shift+Click</b>: Save current image in {@link ImageLoader#OFFLINE_FILES}, (Shift+Ctrl+Click selects and saves both)</li>
 *     <li><b>Alt+Click</b>: Unselect the image (go back to the home page, if images are not loading, click it)</li>
 *     <li></li>
 *     <li><b>Middle Click</b>: Next image (that automatically happens in some time, but to force it)</li>
 *     <li><b>Right Click</b>: Pause it (no refreshes, well, it doesn't quite save resources, so just click it for that)</li>
 *     <li></li>
 *     <li><b>Ctrl+Right Click</b>: Toggle {@link #setFocusableWindowState}</li>
 *     <li><b>Shift+Right Click</b>: Toggle {@link #setAlwaysOnTop}</li>
 *     <li>
 *     <li><b>Alt+Drag</b>: Move the window (also works with Ctrl+Drag)</li>
 *     <li><b>Shift+Drag</b>: Move the image</li>
 *     <li><b>Ctrl+MouseWheel</b>: Zoom the image</li>
 *     <li></li>
 *     <li><b>MouseWheel</b>: Change Brightness</li>
 *     <li><b>Shift+Ctrl+Drag</b>: Change Color Temperature & Tint</li>
 *     <li><b>Shift+Ctrl+Middle Click</b>: Reset Color Temperature & Tint</li>
 * </ul>
 *
 * In case the image failed to load, it will show offline images, in {@link ImageLoader#OFFLINE_FILES}.
 */
// ([M0i]~ <2c> 5c) Decorate5c
public class Decorate5c extends JFrame {
    public static final double L                      = 0.75126;
    public static final double C                      = 0.126;
    public static final int    MIN_POINT_DISTANCE     = 8;
    public static final int    IMAGE_REFRESH_DURATION = 1000*60*5; // 6 min
    public static final int    TRANSITION_DURATION    = 2000;

    public static final  int       w    = 360; // @see ImageLoader#HIGH_RES
    public static final  int       h    = 720;
    private static final Dimension SIZE = new Dimension(w, h + InfoPanel.FONT_SIZE);

    final ArrayList<EffectPanel.Effect> effects = new ArrayList<>();

    BufferedImage image, pImage;
    long pImgTime;
    int  imgX, imgY, pImgX, pImgY;
    float imgScale, pImgScale;
    boolean pause;
    float   brightness  = 1.0f;
    float   temperature = 0.0f;
    float   tint        = 0.0f;


    private Point lastEffect;

    private int mouseX, mouseY;
    private int posX, posY;

    private int mouseImgX, mouseImgY;

    private Decorate5c()
            throws HeadlessException, IOException
    {
        setTitle(Decorate5c.class.getSimpleName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setIcon();

        setSize(SIZE);
        setPreferredSize(SIZE);

        setAlwaysOnTop(false);
        setLocationRelativeTo(null);

        ImageLoader imageLoader = new ImageLoader();

        InfoPanel   infoPanel   = new InfoPanel(this);
        ImagePanel  imagePanel  = new ImagePanel(this);
        EffectPanel effectPanel = new EffectPanel(this);

        Timer timer = createTimer(imageLoader, imagePanel, infoPanel);

        Runnable refreshEffect = new Runnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();

                effects.removeIf(effect -> (effect.startTime() + effect.duration()) < currentTime);

                if (!effects.isEmpty()) {
                    SwingUtilities.invokeLater(this);
                }
                effectPanel.repaint();
            }
        };

        MouseAdapter mouseHandler = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    if (e.isShiftDown()) {
                        setAlwaysOnTop(!isAlwaysOnTop());
                    }
                    if (e.isControlDown()) {
                        setFocusableWindowState(!getFocusableWindowState());
                    }
                    if (!e.isShiftDown() && !e.isControlDown()) {
                        setPause(!pause, timer);
                        infoPanel.setPause(pause);
                    }
                } else if (e.getButton() == MouseEvent.BUTTON2) {
                    if (e.isShiftDown() && e.isControlDown()) {
                        temperature = 0;
                        tint        = 0;
                        imagePanel.repaint();
                    } else if (!pause) timer.restart();
                } else if (e.getButton() == MouseEvent.BUTTON1) {
                    if (pause) return;
                    if (e.isAltDown()) {
                        imageLoader.unselect();
                    } else {
                        long    startTime = System.currentTimeMillis();
                        boolean extra     = false;
                        if (e.isShiftDown()) {
                            if (imageLoader.save(image)) {
                                effects.add(new EffectPanel.Effect(
                                        new Point(e.getX(), e.getY()),
                                        new Color(0xFFFFFF), startTime, 1800
                                ));

                                infoPanel.update(false, imageLoader.isOnline(), imageLoader.isDownloaded(), imageLoader.numImages());
                            }
                        }
                        if (e.isControlDown() && imageLoader.select()) extra = true;

                        if (Math.random() < 0.5) {
                            for (int i = 0; i < heartCoordinates.length/2; i++) {
                                effects.add(new EffectPanel.Effect(
                                        new Point(
                                                (int) (heartCoordinates[2*i]*w/2) + e.getX(),
                                                (int) (heartCoordinates[2*i + 1]*w/2) + e.getY()
                                        ),
                                        new Color(PurityMaths.oklchToSrgb(L, C, Math.random()*30 - 15)),
                                        startTime, (int) (1000 + Math.random()*500)
                                ));
                            }

                            if (extra) {
                                for (int i = 0; i < heartCoordinates.length/2; i++) {
                                    effects.add(new EffectPanel.Effect(
                                            new Point(
                                                    (int) (heartCoordinates[2*i]*w*2/3) + e.getX(),
                                                    (int) (heartCoordinates[2*i + 1]*w*2/3) + e.getY()
                                            ),
                                            new Color(PurityMaths.oklchToSrgb(L, C, Math.random()*30 - 15)),
                                            startTime, (int) (500 + Math.random()*200)
                                    ));
                                }
                            }
                        } else {
                            int angle = (int) (Math.random()*360);
                            for (int i = 0; i < 24; i++) {
                                effects.add(new EffectPanel.Effect(
                                        new Point(
                                                (int) (Math.cos(2*Math.PI*i/24)*w/3) + e.getX(),
                                                (int) (Math.sin(2*Math.PI*i/24)*w/3) + e.getY()
                                        ),
                                        new Color(PurityMaths.oklchToSrgb(L, C, angle + Math.random()*8 + 360*i/24f)),
                                        startTime, (int) (1400 + Math.random()*700)
                                ));
                            }

                            if (extra) {
                                for (int i = 0; i < 24; i++) {
                                    effects.add(new EffectPanel.Effect(
                                                        new Point(
                                                                (int) (Math.cos(2*Math.PI*i/24)*w/2) + e.getX(),
                                                                (int) (Math.sin(2*Math.PI*i/24)*w/2) + e.getY()
                                                        ),
                                                        new Color(PurityMaths.oklchToSrgb(L, C, angle + 360*i/24f)),
                                                        startTime, 800
                                                )
                                    );
                                }
                            }
                        }
                        SwingUtilities.invokeLater(refreshEffect);
                    }
                }
            }

            @Override
            public void mouseWheelMoved(final MouseWheelEvent e) {
                super.mouseWheelMoved(e);
                if (e.isControlDown()) imgScale = Math.max(imgScale + -e.getWheelRotation()*0.07f, 1);
                else brightness = Math.clamp(brightness + e.getWheelRotation()*0.07f, 0, 1);
                imagePanel.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mouseX    = e.getX();
                mouseY    = e.getY();
                mouseImgX = e.getXOnScreen();
                mouseImgY = e.getYOnScreen();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (e.isShiftDown()) {
                    int x = e.getXOnScreen();
                    int y = e.getYOnScreen();

                    if (e.isControlDown()) {
                        temperature = Math.clamp(temperature + (float) (x - mouseImgX)/w, -1f, 1f);
                        tint        = Math.clamp(tint + (float) (y - mouseImgY)/w, -1f, 1f);
                    } else {
                        imgX += x - mouseImgX;
                        imgY += y - mouseImgY;
                    }

                    mouseImgX = e.getXOnScreen();
                    mouseImgY = e.getYOnScreen();

                    imagePanel.repaint();
                } else if (e.isControlDown() || e.isAltDown()) {
                    int x = e.getXOnScreen();
                    int y = e.getYOnScreen();

                    posX = x - mouseX;
                    posY = y - mouseY;

                    setLocation(posX, posY);
                } else if (lastEffect == null || lastEffect.distance(e.getPoint()) > MIN_POINT_DISTANCE*MIN_POINT_DISTANCE) {
                    lastEffect = e.getPoint();
                    effects.add(new EffectPanel.Effect(
                            e.getPoint(),
                            new Color(PurityMaths.oklchToSrgb(L, C, Math.random()*360)),
                            System.currentTimeMillis(), (int) (1000 + 100*Math.random())
                    ));
                    SwingUtilities.invokeLater(refreshEffect);
                }
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        addMouseWheelListener(mouseHandler);

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 180));

        setLayout(null);
        imagePanel.setBounds(0, 0, w, h);
        effectPanel.setBounds(0, 0, w, h);
        infoPanel.setBounds(0, 0, w, h + InfoPanel.FONT_SIZE);
        add(effectPanel);
        add(imagePanel);
        add(infoPanel);
        infoPanel.setOpaque(false);
        imagePanel.setOpaque(false);
        effectPanel.setOpaque(false);

        setPause(false, timer);
    }

    public static void main(String[] args)
            throws Exception
    {
        new Decorate5c().setVisible(true);
    }

    private Timer createTimer(final ImageLoader imageLoader, final ImagePanel imagePanel, final InfoPanel infoPanel) {
        Timer timer = new Timer(
                IMAGE_REFRESH_DURATION, _ -> {
            long t = System.currentTimeMillis();
            if (t < pImgTime + TRANSITION_DURATION) {
                return;
            }

            pImgTime  = t;
            pImgX     = imgX;
            pImgY     = imgY;
            pImgScale = imgScale;
            pImage    = image;

            imgX     = imgY = 0;
            imgScale = 24/23f;
            image    = imageLoader.nextImage();

            infoPanel.update(true, imageLoader.isOnline(), imageLoader.isDownloaded(), imageLoader.numImages());

            imagePanel.repaint();
        }
        );
        timer.setInitialDelay(0);
        return timer;
    }

    private void setIcon() {
        final int     s    = 256;
        BufferedImage icon = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D    g    = icon.createGraphics();
        for (int i = 0; i < 6; i++) {
            g.setColor(new Color(PurityMaths.oklchToSrgb(L, C, 8.74 + i*60)));
            g.fillArc(0, 0, s, s, i*60 - 26, 52);
        }

        g.setComposite(AlphaComposite.Src);
        g.setColor(new Color(0, 0, 0, 0));
        g.fillOval(s/7, s/7, s - 2*s/7, s - 2*s/7);

        g.setColor(new Color(PurityMaths.oklchToSrgb(L, 0, 0)));
        g.fillOval(s/5, s/5, s - 2*s/5, s - 2*s/5);

        g.dispose();
        setIconImages(List.of(icon));
    }

    private void setPause(boolean shallPause, Timer timer) {
        pause = shallPause;

        if (pause) {
            timer.stop();
        } else {
            effects.clear();
            if (image != null) timer.setInitialDelay(IMAGE_REFRESH_DURATION);
            timer.start();
            timer.setInitialDelay(0);
        }

        repaint();
    }
}
