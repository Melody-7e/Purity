package com.ri;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

// ([M0i]~ <2c> 4a) Secrets4a
public class Secrets4a extends JFrame {
    private final String         archive;
    private final String         file;
    private final String         archiveName;
    private final JPasswordField password;
    private final JButton        exitButton;

    private String  msgOut;
    private String  msgIn;
    private boolean cleaned;

    private boolean ignoreDeleteErrors;

    private Secrets4a(String archive, String file)
    {
        this.archive = archive;
        this.file    = file;
        String name = Path.of(archive).getFileName().toString();
        if (name.endsWith(".7z")) name = name.substring(0, name.length() - 3);
        this.archiveName = name;

        UIManager.put("Button.select", new Color(0f, 0.6f, 0f));

        setTitle(Secrets4a.class.getSimpleName());
        setSize(360, 40);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Border focusIn = new CompoundBorder(new LineBorder(new Color(0f, 1f, 0.1f)), new EmptyBorder(5, 11, 5, 11));
        Border focusOt = new EmptyBorder(6, 12, 6, 12);

        exitButton = new JButton();
        exitButton.setFocusPainted(false);
        exitButton.setHorizontalAlignment(JTextField.CENTER);
        exitButton.setBackground(new Color(0f, 0f, 0f));
        exitButton.setForeground(new Color(0.1f, 1f, 0.2f));
        exitButton.setBorder(focusIn);
        exitButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                exitButton.setText(msgOut);
                exitButton.setBackground(new Color(0f, 0f, 0f));
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                exitButton.setText(msgIn);
                exitButton.setBackground(new Color(0f, 0.3f, 0f));
            }
        });
        setMessage("", null, "");

        password = new JPasswordField();
        password.addActionListener(e -> {
            Thread t = new Thread(() -> start(e.getActionCommand()));
            t.start();
        });
        password.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                password.setBorder(focusIn);
            }

            @Override
            public void focusLost(FocusEvent e) {
                password.setBorder(focusOt);
            }
        });
        password.setBackground(new Color(0f, 0f, 0f));
        password.setForeground(new Color(0.1f, 1f, 0.2f));
        password.setCaretColor(new Color(0.15f, 1f, 0.3f));
        password.setSelectionColor(new Color(0f, 0.4f, 0f));
        add(password);

        setLocationRelativeTo(null);
        setUndecorated(true);
        setVisible(true);
    }

    private void setMessage(String msgOut, String error, String msgIn) {
        this.msgOut = msgOut;
        this.msgIn = msgIn;
        exitButton.setText(msgOut);

        if (error != null) {
            exitButton.addActionListener(_ -> System.exit(0));
            JOptionPane.showMessageDialog(this, error, msgOut, JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Invalid number of arguments, required 2 (archive path, start file)");
            System.out.println("Usage: javaw Secrets4a <archive> <file>");
            return;
        }

        SwingUtilities.invokeLater(() -> new Secrets4a(args[0], args[1]));
    }

    private void start(String password) {
        setUpExitButton();

        Path tmpDir;
        try {
            tmpDir = Files.createTempDirectory(archiveName).toAbsolutePath();
        } catch (Exception e) {
            setMessage(e.getClass().getSimpleName(), e.getMessage(), "Exit");
            return;
        }

        setMessage("Extracting...", null, tmpDir.getFileName().toString());
        String errX = runCmd("7z", "x", archive, "-p" + password, "-o" + tmpDir, "-y");
        if (errX != null) {
            deleteDir(tmpDir.toFile());
            setMessage("Extraction Failed", errX, "Exit");
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> cleanUp(password, tmpDir)));
        exitButton.addActionListener(_ -> {
            setMessage("Cleaning Up...", null, tmpDir.getFileName().toString());
            if (!cleaned) {
                new Thread(() -> cleanUp(password, tmpDir)).start();
            }
        });

        setMessage("Opening...", null, file);
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.open(new File(tmpDir.toFile(), file));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
        }

        setMessage(archiveName, null, "Exit");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width - getWidth() - 16, 24);
    }

    private void cleanUp(String password, Path tmpDir) {
        if (cleaned) return;
        cleaned = true;

        Path tmp7z;
        try {
            tmp7z = Files.createTempFile(archiveName, ".7z").toAbsolutePath();
            Files.delete(tmp7z);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String errA = runCmd("7z", "a", tmp7z.toString(), "-p" + password, "-mhe", "-mx0", tmpDir + "/*");
        if (errA != null) {
            JOptionPane.showMessageDialog(null, errA, "Compression failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Files.move(tmp7z, Path.of(archive), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        deleteDir(tmpDir.toFile());

        System.exit(0);
    }

    private void setUpExitButton() {
        remove(this.password);
        add(exitButton);
        invalidate();
        revalidate();
        repaint();
    }

    private void deleteDir(File dir) {
        File[] files = dir.listFiles();
        if (files != null) for (File f: files) deleteDir(f);
        if (!dir.delete() && !ignoreDeleteErrors) {
            int out = JOptionPane.showOptionDialog(this, "Cannot delete " + dir + "\nRetry?", "IOException", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[]{"Yes", "No", "No (for all others)"}, "Yes");
            if (out == JOptionPane.YES_OPTION) {
                deleteDir(dir);
            } else if (out == JOptionPane.CANCEL_OPTION) {
                ignoreDeleteErrors = true;
            }
        }
    }

    private String runCmd(String... args) {
        try {
            Process process = new ProcessBuilder(args).start();
            int     exit    = process.waitFor();
            String  err     = new String(process.getErrorStream().readAllBytes());
            return exit == 0? null : err;
        } catch (Exception e) {return e.getMessage();}
    }
}
