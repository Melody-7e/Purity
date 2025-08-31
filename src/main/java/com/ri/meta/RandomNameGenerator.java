package com.ri.meta;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.Random;

public class RandomNameGenerator {
    public static void main(String[] args) {
        final String name = getNewName();
        System.out.println(name);

        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(name), null);
    }

    public static String getNewName() {
        Random r = new Random();

        StringBuilder sb = new StringBuilder();

        int l1 = r.nextInt(2, 4);         // 2-3
        int l2 = r.nextInt(3, 7 - l1);    // 3-4
        int l3 = r.nextInt(5 - l2, 3);    // 1-2

        for (int i = 0; i < l1; i++) {
            sb.append(getAlpha(r, i == 0));
        }
        sb.append('_');

        for (int i = 0; i < l2; i++) {
            sb.append(getAlpha(r, i == 0));
        }
        sb.append('_');

        for (int i = 0; i < l3; i++) {
            sb.append(getNumAlpha(r, i == 0));
        }

        return sb.toString();
    }

    private static char getAlpha(Random r, boolean upper) {
        if (upper) return (char) ('A' + r.nextInt(26));
        return (char) ('a' + r.nextInt(26));
    }

    private static char getNumAlpha(Random r, boolean alpha) {
        if (alpha) return (char) ('A' + r.nextInt(26));
        return (char) ('0' + r.nextInt(10));
    }
}
