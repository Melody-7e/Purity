package com.ri;

import com.ri.helper.PurityMaths;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.Timer;

// ([M0i]~ <2c> 4c) Reminder4c
public class Reminder4c extends JFrame {
    public static final double L = 0.75126;
    public static final double C = 0.126;

    private static final int       s       = 256;
    private static final Dimension SIZE    = new Dimension(s, s + 16);
    private static final Stroke    stroke5 = new BasicStroke(5.0f);

    private static final PriorityQueue<Event> events = new PriorityQueue<>(Comparator.comparingInt(o -> o.startTime));

    private static final Font           FONT     = new Font("Arial", Font.PLAIN, 16);
    private static final Font           FONT2    = new Font("Arial", Font.PLAIN, 48);

    static {
        events.add(new Event("-20-", 0.0f, 20*60, 20, 2*60, 0));
        // events.add(new Event("-57-", 150f, 57*60, 37, 8*60, 7));
        // events.add(new Event("Rand", 240f, 30*60, 72, 60*60, 40));
        // events.add(new Event("_Fcs", 51.f, 40*60, 2*60, 6*60, 60));
    }

    private final Random random = new Random();

    private int mouseX, mouseY;
    private int posX, posY;

    private boolean hold;
    private String  eventName;
    private int     eventTime;
    private int     eventDelay;
    private int     eventDuration;
    private float   eventHue;
    private long    startTime;
    private long    pauseTime;
    private boolean block = false;
    private boolean pause = false;

    private Reminder4c()
            throws HeadlessException
    {
        this.setTitle(Reminder4c.class.getSimpleName());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BufferedImage icon = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D    g    = icon.createGraphics();
        for (int i = 0; i < 6; i++) {
            g.setColor(new Color(PurityMaths.oklchToSrgb(L, C, 8.74 + i*60)));
            g.fillArc(0, 0, s, s, i*60 - 26, 52);
        }

        g.setComposite(AlphaComposite.Src);
        g.setColor(new Color(0, 0, 0, 0));
        g.fillOval(s/7, s/7, s - 2*s/7, s - 2*s/7);

        g.dispose();
        setIconImages(List.of(
                icon,
                icon.getScaledInstance(128, 128, Image.SCALE_SMOOTH),
                icon.getScaledInstance(72, 72, Image.SCALE_SMOOTH),
                icon.getScaledInstance(64, 64, Image.SCALE_SMOOTH),
                icon.getScaledInstance(32, 32, Image.SCALE_SMOOTH),
                icon.getScaledInstance(24, 24, Image.SCALE_SMOOTH)
        ));

        setSize(SIZE);
        setMinimumSize(SIZE);
        setMaximumSize(SIZE);
        setPreferredSize(SIZE);

        setAlwaysOnTop(true);
        setLocationRelativeTo(null);
        // setType(Type.UTILITY);

        Timer timer = new Timer(
                60,
                _ -> {
                    float t = (System.currentTimeMillis() - startTime)/1000f;
                    if (t > eventTime + eventDuration) nextEvent();
                    repaint();
                }
        );
        MouseAdapter mouseHandler = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (!pause && e.getButton() == MouseEvent.BUTTON3) {
                    setBlock(!block);
                } else if (e.getButton() == MouseEvent.BUTTON2) {
                    setPause(!pause, timer);
                }
            }

            public void mouseReleased(MouseEvent e) {
                hold = false;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();

                if (block) {
                    mouseX -= posX;
                    mouseY -= posY;
                }

                hold = true;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getXOnScreen();
                int y = e.getYOnScreen();

                posX = x - mouseX;
                posY = y - mouseY;

                if (!block) {
                    setLocation(posX, posY);
                }
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        startTime = System.currentTimeMillis() - 1000;
        nextEvent();

        timer.start();
    }

    public static void main(String[] args)
            throws Exception
    {
        new Reminder4c().setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        float t = (System.currentTimeMillis() - startTime)/1000f;
        float A = eventTime - eventDelay/12f;
        float B = eventTime - eventDelay/36f;

        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        ((Graphics2D) g).setComposite(AlphaComposite.Src);
        ((Graphics2D) g).setStroke(stroke5);

        if (block) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());

            g.translate(posX, posY);
        }

        if (pause) {
            g.setColor(Color.GRAY);
            g.fillOval(0, 0, s, s);

            if (block)
                g.setColor(Color.BLACK);
            else
                g.setColor(new Color(0, 0, 0, 0));

            g.fillOval(s/7, s/7, s - 2*s/7, s - 2*s/7);
            return;
        }

        for (int i = 0; i < 12; i++) {
            if (t > B && t < eventTime && (int) ((eventTime - t)/(eventTime - B)*12) == i) continue;
            if (t > eventTime && (int) ((eventDuration - t + eventTime)/(eventDuration)*12) != i) continue;

            g.setColor(new Color(PurityMaths.oklchToSrgb(L, C, t*23.7 + i*30)));
            g.fillArc(0, 0, s, s, i*30 - 14, 28);
        }

        g.setColor(Color.WHITE);
        if (eventDuration > 24 && t > eventTime) {
            g.drawArc(
                    s/7, s/7, s - 2*s/7, s - 2*s/7,
                    ((int) ((eventDuration - t + eventTime)/(eventDuration)*12*13)%12)*30 - 14, 28
            );
        }
        if (eventDuration > 24*24 && t > eventTime) {
            g.drawArc(
                    s/9, s/9, s - 2*s/9, s - 2*s/9,
                    ((int) ((eventDuration - t + eventTime)/(eventDuration)*12*13*13)%12)*30 - 14, 28
            );
        }

        if (block)
            g.setColor(Color.BLACK);
        else
            g.setColor(new Color(0, 0, 0, 0));

        g.fillOval(s/7, s/7, s - 2*s/7, s - 2*s/7);


        int   color;
        float intensity;


        if (t < A) {
            color     = PurityMaths.oklchToSrgb(L, 0, 0);
            intensity = 0.5f;
        } else if (t < B) {
            color     = PurityMaths.oklchToSrgb(L, C*(t - A)/(B - A), eventHue);
            intensity = 0.5f;
        } else if (t < eventTime) {
            color = PurityMaths.oklchToSrgb(L, C, eventHue);
            float k = (t - B)/(eventTime - B);
            intensity = 0.5f*(1 - k) + 1.5f*k;
        } else if (t < eventTime + eventDuration) {
            color     = PurityMaths.oklchToSrgb(L, C, eventHue);
            intensity = 2f;
        } else {
            nextEvent();
            color     = PurityMaths.oklchToSrgb(L, 0, 0);
            intensity = 0.5f;
        }

        ((Graphics2D) g).setComposite(AlphaComposite.SrcOver);
        for (int i = 0; i < 3; i++) {
            float k = (t/4f + i/3f)%1;

            float r = 1/6f + 5/6f*k/3;
            g.setColor(new Color(color | ((int) (k*(1 - k)*intensity*255) << 24), true));

            if (t < eventTime) {
                g.drawOval((int) (s*r), (int) (s*r), (int) (s - 2*s*r), (int) (s - 2*s*r));
            } else {
                g.fillOval((int) (s*r), (int) (s*r), (int) (s - 2*s*r), (int) (s - 2*s*r));
            }
        }

        if (hold) {
            if (t > eventTime) {
                g.setColor(new Color(PurityMaths.oklchToSrgb(L, C, eventHue + 27)));
                g.setFont(FONT2);
                String str = String.valueOf((int) (eventDuration - t + eventTime + 1));
                g.drawString(
                        str,
                        s/2 - getFontMetrics(FONT2).stringWidth(str)/2,
                        s/2 + getFontMetrics(FONT2).getAscent()/2
                );
            } else if (t > B) {
                g.setColor(new Color(PurityMaths.oklchToSrgb(L, C, eventHue - 23)));
                g.setFont(FONT2);
                String str = String.valueOf((int) (eventTime - t + 1));
                g.drawString(
                        str,
                        s/2 - getFontMetrics(FONT2).stringWidth(str)/2,
                        s/2 + getFontMetrics(FONT2).getAscent()/2
                );
            }
        }

        if (hold) {
            g.setFont(FONT);
            g.setColor(Color.WHITE);
            g.drawString(eventName, s/2 - getFontMetrics(FONT).stringWidth(eventName)/2, s + 16);
        }
    }

    private void setPause(boolean shallPause, Timer timer) {
        pause = shallPause;

        if (pause) {
            setBlock(false);

            pauseTime = System.currentTimeMillis();
            timer.stop();
            repaint();
        } else {
            startTime += System.currentTimeMillis() - pauseTime;
            timer.restart();
        }
    }

    private void setBlock(boolean shallBlock) {
        block = shallBlock;
        setExtendedState(block? MAXIMIZED_BOTH : NORMAL);

        if (!block) {
            setLocation(posX, posY);
        }
    }

    private void nextEvent() {
        int t = Math.toIntExact((System.currentTimeMillis() - startTime)/1000);

        Event event = events.poll();
        assert event != null;

        eventName     = event.name;
        eventDelay    = event.delay + random.nextInt(event.randomnessDelay + 1);
        eventDuration = event.duration + random.nextInt(event.randomnessDuration + 1);
        eventTime     = Math.max(t + eventDelay/24 + 2, event.startTime);
        eventHue      = event.hue;

        event.startTime += eventDelay;
        events.add(event);
    }

    static final class Event {
        private final String name;
        private final float  hue;
        private final int    delay;
        private final int    duration;
        private final int    randomnessDelay;
        private final int    randomnessDuration;
        private       int    startTime;

        Event(String name, float hue, int delay, int duration, int randomnessDelay, int randomnessDuration) {
            if (delay < randomnessDelay/2 || duration < randomnessDuration/2) {
                throw new RuntimeException();
            }

            this.name               = name;
            this.hue                = hue;
            this.delay              = delay - randomnessDelay/2;
            this.duration           = duration - randomnessDuration/2;
            this.randomnessDelay    = randomnessDelay;
            this.randomnessDuration = randomnessDuration;
            this.startTime          = this.delay + (int) ((Math.random())*randomnessDelay);
        }
    }
}
