package com.ri.decorate5c;

import java.awt.*;

import javax.swing.JLabel;
import javax.swing.JPanel;

class InfoPanel extends JPanel {
    static final         int  FONT_SIZE = 16;
    private static final Font FONT      = new Font("Arial", Font.PLAIN, FONT_SIZE);
    private final JLabel pausedLabel;
    private final JLabel infoLabel;

    private final int[] colors = {
            0x91afe2,
            0xd59ab7,
            0xc9a973,
            0x7abfa5,
    };

    private int imageIndex = 0;

    public InfoPanel(final Decorate5c decorate5c) {
        setLayout(null);

        pausedLabel = new JLabel("""
                                 <html>
                                 <ul>
                                 <li><b style="color: #f28b9d">Click</b>:<br> Draws heart/ or circle</li>
                                 <li><b style="color: #f28b9d">Drag</b>:<br> Draws a line of hearts</li>
                                 <br>
                                 <li><b style="color: #95be65">Ctrl+Click</b>:<br> Select the image</li>
                                 <li><b style="color: #95be65">Shift+Click</b>:<br> Save current image</li>
                                 <li><b style="color: #95be65">Alt+Click</b>:<br> Unselect the image</li>
                                 <br>
                                 <li><b style="color: #c698eb">Middle Click</b>:<br> Next image</li>
                                 <li><b style="color: #c698eb">Right Click</b>:<br> Pause it</li>
                                 <br>
                                 <li><b style="color: #c6ae45">Ctrl+Right Click</b>:<br> Toggle FocusableWindowState</li>
                                 <li><b style="color: #c6ae45">Shift+Right Click</b>:<br> Toggle AlwaysOnTop</li>
                                 <br>
                                 <li><b style="color: #9aa7fd">Alt+Drag</b>:<br> Move the window</li>
                                 <li><b style="color: #9aa7fd">Shift+Drag</b>:<br> Move the image</li>
                                 <li><b style="color: #9aa7fd">Ctrl+MouseWheel</b>:<br> Zoom the image</li>
                                 <br>
                                 <li><b style="color: #f29075">MouseWheel</b>:<br> Change Brightness</li>
                                 <li><b style="color: #f29075">Shift+Ctrl+Drag</b>:<br> Change Temperature & Tint</li>
                                 <li><b style="color: #f29075">Shift+Ctrl+Middle Click</b>:<br> Reset Temperature & Tint</li>
                                 </ul>
                                 </html>
                                 """);
        pausedLabel.setForeground(new Color(0xc7c7c7));
        pausedLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Decorate5c.h/47));
        pausedLabel.setBounds(0, 0, Decorate5c.w, Decorate5c.h);
        add(pausedLabel);

        infoLabel = new JLabel("<init>");
        infoLabel.setBounds(0, Decorate5c.h, Decorate5c.w, FONT_SIZE);
        infoLabel.setForeground(new Color(0xc7c7c7));
        infoLabel.setFont(FONT);
        add(infoLabel);
    }

    @Override
    public void paint(final Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paint(g);
    }

    void setPause(boolean isPaused) {
        pausedLabel.setVisible(isPaused);
        infoLabel.setVisible(!isPaused);
    }

    void update(boolean increment, boolean isOnline, boolean isDownloaded, int numImages) {
        if (increment) imageIndex++;

        infoLabel.setText(String.format(
                """
                <html>
                <body>
                <pre>
                Image: <b style="color: #%x">%3s(%s)</b> <span style="color: #95be65">%-10s</span> Loaded: <span style="color: #9aa7fd">%2s</span>|
                </pre>
                </body>
                </html>
                """, colors[imageIndex % 4], imageIndex, imageIndex%4,
                isOnline? (isDownloaded? "downloaded": ""): "offline",
                numImages
        ));
    }
}