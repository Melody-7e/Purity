package com.ri;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;

// ([M0i]~ <2c> 42) LetsCatchStalkers42
public class LetsCatchStalkers42 extends JFrame {
    boolean executedOnce = false;
    int     screenShotIndex;

    ExecutorService service = Executors.newSingleThreadExecutor();

    public static void main(String[] args)
            throws Exception
    {
        SwingUtilities.invokeLater(LetsCatchStalkers42::new);
    }

    public LetsCatchStalkers42()
    {
        setTitle(LetsCatchStalkers42.class.getSimpleName());
        setLayout(null);
        setUndecorated(true);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        setLocation(0, 0);
        setAlwaysOnTop(true);
        setBackground(new Color(200, 0, 160, 64));
        setType(Type.UTILITY);
        setSize(16, 16);

        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        setVisible(true);
        requestFocus();

        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        KeyStroke ctrlQ = KeyStroke.getKeyStroke("control Q");
        String actionKey = "handleCtrlQ";
        inputMap.put(ctrlQ, actionKey);

        getRootPane().getActionMap().put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                onStalkerFound();
            }
        });
    }

    void onStalkerFound()
    {
        if (executedOnce) return;
        executedOnce = true;

        setBackground(new Color(0, 200, 47, 64));

        Toolkit tk = Toolkit.getDefaultToolkit();
        tk.beep();

        try {
            File  directory       = Files.createTempDirectory("LetsCatchStalkers42").toFile();
            Timer screenshotTimer = new Timer(500, e -> screenShot(directory, ((Timer) e.getSource())));
            screenshotTimer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void screenShot(File directory, Timer screenshotTimer) {
        if (screenShotIndex > 42) System.exit(0);
        service.execute(() -> {
            try {
                Robot robot = new Robot();

                Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                BufferedImage img = robot.createScreenCapture(screenRect);

                System.out.println(screenShotIndex);

                ImageIO.write(img, "png", new File(directory, (screenShotIndex++) + ".png"));
            } catch (AWTException | IOException e) {
                throw new RuntimeException(e);
            }
        });

        if (screenShotIndex == 32) {
            screenshotTimer.stop();

            Robot robot;
            try {
                robot = new Robot();
            } catch (AWTException e) {
                throw new RuntimeException(e);
            }

            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage img = robot.createScreenCapture(screenRect);

            SmoothDistortedPanel panel = new SmoothDistortedPanel(img, directory);
            panel.setBounds(0, 0, img.getWidth(), img.getHeight());
            add(panel);

            Timer timer = new Timer(
                    60, e -> {
                if (panel.isShowing()) {
                    panel.repaint();
                } else {
                    ((Timer) e.getSource()).stop();
                }
            }
            );
            timer.start();

            getRootPane().setWindowDecorationStyle(JRootPane.NONE);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            requestFocus();
        }
    }
}

class SmoothDistortedPanel extends JPanel {
    private final Random        rand  = new Random();
    private final BufferedImage screenSource;
    private final File          directory;
    private       int           frame = 0;


    public SmoothDistortedPanel(BufferedImage sourceImage, File directory) {
        this.screenSource = sourceImage;
        this.directory    = directory;

        setDoubleBuffered(true);

        Timer timer = new Timer(60, _ -> repaint());
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        frame++;

        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        if (w <= 0 || h <= 0) return;

        if (frame == 55) {
            try {
                Runtime.getRuntime().exec(new String[]{"powershell.exe", "(Get-WmiObject -Namespace root/WMI -Class WmiMonitorBrightnessMethods).WmiSetBrightness(1,100)"});
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (frame < 60) {
            g.drawImage(applyGPUGlitch(screenSource, w, h), 0, 0, null);
        } else if (frame < 210 && (Math.random()*3/2 < (frame - 60)/(210 - 60f))) {
            Toolkit.getDefaultToolkit().beep(); // quick static noise-like sound
            g.drawImage(applyGPUGlitch(screenSource, w, h), 0, 0, null);
        } else if (frame < 230) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, w, h);
        } else if (frame == 251) {
            try {
                Runtime.getRuntime().exec(new String[]{"powershell.exe", "(Get-WmiObject -Namespace root/WMI -Class WmiMonitorBrightnessMethods).WmiSetBrightness(1,0)"});
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (frame == 291) {
            try {
                Runtime.getRuntime().exec(new String[]{"rundll32.exe", "user32.dll,LockWorkStation"});
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            JFrame frame = (JFrame) getRootPane().getParent();
            frame.remove(this);
            frame.setExtendedState(JFrame.NORMAL);
            frame.setBackground(Color.WHITE);

            JLabel comp = new JLabel("We catch a stalker... See: " + directory);
            Toolkit.getDefaultToolkit().getSystemClipboard()
                   .setContents(new StringSelection(directory.getAbsolutePath()), null);
            comp.setBounds(32, 0, 800, 100);
            frame.add(comp);
            frame.getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
            frame.setSize(800, 100);
        } else if (frame > 301) {
            System.exit(0);
        }
    }

    private BufferedImage applyGPUGlitch(BufferedImage src, int targetW, int targetH) {
        BufferedImage dest = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);

        double glitchIntensity = Math.min(frame*0.005, 1.0);

        double scaleX = (double) src.getWidth()/targetW;
        double scaleY = (double) src.getHeight()/targetH;

        for (int y = 0; y < targetH; y++) {
            int shiftX = 0;

            if (rand.nextDouble() < (0.05*glitchIntensity)) {
                shiftX = rand.nextInt((int) (50*glitchIntensity + 1)) - (int) (25*glitchIntensity);
            } else if (rand.nextDouble() < (0.3*glitchIntensity)) {
                shiftX = (int) (5*Math.sin(y*0.5)*glitchIntensity);
            }

            for (int x = 0; x < targetW; x++) {
                int srcX = (int) ((x + shiftX)*scaleX);
                int srcY = (int) (y*scaleY);

                srcX = Math.clamp(srcX, 0, screenSource.getWidth() - 1);
                srcY = Math.clamp(srcY, 0, screenSource.getHeight() - 1);

                int originalRGB = src.getRGB(srcX, srcY);

                if (glitchIntensity > 0.2 && rand.nextDouble() < (0.15*glitchIntensity)) {
                    int rgbShift = (int) (15*glitchIntensity);
                    int rX       = Math.clamp(srcX + rgbShift, 0, src.getWidth() - 1);
                    int bX       = Math.clamp(srcX - rgbShift, 0, src.getWidth() - 1);

                    int rColor = src.getRGB(rX, srcY);
                    int bColor = src.getRGB(bX, srcY);

                    int r = (rColor >> 16) & 0xFF;
                    int g = (originalRGB >> 8) & 0xFF;
                    int b = bColor & 0xFF;
                    int a = (originalRGB >> 24) & 0xFF;

                    dest.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
                } else {
                    dest.setRGB(x, y, originalRGB);
                }
            }
        }

        int numBlocks = (int) (9*glitchIntensity);
        for (int i = 0; i < numBlocks; i++) {
            if (rand.nextDouble() < glitchIntensity) {
                int blockW = rand.nextInt((int) (450*glitchIntensity + 10));
                int blockH = rand.nextInt((int) (125*glitchIntensity + 5));
                int blockX = rand.nextInt(targetW);
                int blockY = rand.nextInt(targetH);

                boolean isColorBlock = rand.nextBoolean();
                Color   glitchColor  = rand.nextBoolean()? Color.GREEN : Color.MAGENTA;

                for (int by = blockY; by < blockY + blockH && by < targetH; by++) {
                    for (int bx = blockX; bx < blockX + blockW && bx < targetW; bx++) {
                        if (isColorBlock) {
                            dest.setRGB(bx, by, glitchColor.getRGB());
                        } else {
                            int n = rand.nextInt(256);
                            dest.setRGB(bx, by, (255 << 24) | (n << 16) | (n << 8) | n);
                        }
                        src.setRGB(bx, by, 0);
                    }
                }
            }
        }

        if (frame%40 == 20) {
            int   lineX       = rand.nextInt(targetW);
            Color glitchColor = rand.nextBoolean()? Color.GREEN : Color.MAGENTA;
            for (int y = 0; y < targetH; y++) {
                src.setRGB(lineX, y, glitchColor.getRGB());
            }
        }

        return dest;
    }
}
