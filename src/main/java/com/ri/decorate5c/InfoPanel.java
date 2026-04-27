package com.ri.decorate5c;

import java.awt.*;

import javax.swing.JLabel;
import javax.swing.JPanel;

class InfoPanel extends JPanel {
    private final Decorate5c decorate5c;

    public InfoPanel(final Decorate5c decorate5c) {
        this.decorate5c = decorate5c;

        JLabel label = new JLabel("""
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
        label.setForeground(new Color(0xc7c7c7));
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Decorate5c.h / 47));
        add(label);
    }

    @Override
    public void paint(Graphics graphics) {
        if (!decorate5c.pause) return;
        super.paint(graphics);
    }
}