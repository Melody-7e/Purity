package com.ri;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;


// ([M0i]~ <2c> a4) MemorizeA4
public class MemorizeA4 extends JFrame {
    private static final String PHI = "1.6180339887498948482045868343656381177203091798057628621354486227052604628189024497072072041893911375";
    private static final String E   = "2.7182818284590452353602874713526624977572470936999595749669676277240766303535475945713821785251664274";
    private static final String PI  = "3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679";

    private String sequence;
    private int    position     = 1;
    private int    showPosition = 1;

    public MemorizeA4()
    {
        setTitle(MemorizeA4.class.getSimpleName());
        setLayout(null);
        setUndecorated(true);

        JTextField field = new JTextField();
        field.setBounds(20, 10, 570, 60);
        field.setHorizontalAlignment(JTextField.RIGHT);
        field.setFont(new Font("Google Sans Code Light",  Font.PLAIN, 42));
        field.setBackground(new Color(0xE9C7FF));
        field.addActionListener(_ -> fieldAction(field));
        add(field);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(610, 110);
        setResizable(false);
        setDefaultLookAndFeelDecorated(true);
        setLocationRelativeTo(null);
        getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);

        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        setVisible(true);
    }

    public static void main(String[] args)
            throws Exception
    {
        SwingUtilities.invokeLater(MemorizeA4::new);
    }

    int mismatch(String a, String b) {
        int length = Math.min(a.length(), b.length());

        for (int i = 0; i < length; i++) {
            if (a.charAt(i) != b.charAt(i)) return i;
        }
        return length;
    }

    private void fieldAction(JTextField field) {
        if (!field.isEditable()) {
            return;
        }

        if (sequence == null) {
            sequence = field.getText();

            switch (sequence) {
                case "\\PI" -> sequence = PI;
                case "\\PHI" -> sequence = PHI;
                case "\\E" -> sequence = E;
            }

            field.setBackground(new Color(0xC7FFCE));
        } else if (field.getText().length() >= position && sequence.startsWith(field.getText())) {
            field.setBackground(new Color(0xC7FFCE));

            if (position >= sequence.length()) field.setBackground(new Color(0xBDFFB4));
            else position = Math.min(field.getText().length() + 1, sequence.length());
        } else {
            position = Math.min(mismatch(sequence, field.getText()) + 1, sequence.length());

            field.setBackground(new Color(0xFFC7DC));
        }

        field.setText("");
        field.setEditable(false);

        Timer timer = new Timer(
                250, e -> {
            if (showPosition <= position) {
                field.setText(sequence.substring(0, showPosition));
            } else if (showPosition == position + 2) {
                field.setText("");
            } else if (showPosition == position + 4) {
                showPosition = 0;
                field.setBackground(new Color(0xC7E1EF));
                field.setEditable(true);
                ((Timer) e.getSource()).stop();
            }

            showPosition++;
        }
        );

        timer.start();
    }
}
