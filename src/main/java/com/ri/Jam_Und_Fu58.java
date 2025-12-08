package com.ri;

import com.ri.meta.*;

import java.io.PrintStream;
import java.util.Random;

public class Jam_Und_Fu58 {
    @SuppressWarnings("UnnecessaryLocalVariable")
    public static void main(String[] args) throws Exception {
        // @formatter:off
        String          _clazzName  = Jam_Und_Fu58.class.getSimpleName();
        ProjectType     type        = ProjectType.DATA;
        ProjectPD       pd          = ProjectPD.LEFT_MID;
        ProjectCategory category    = ProjectCategory.CLASS_S;
        byte            id          = (byte) Integer.parseInt(_clazzName.substring(_clazzName.length() - 2), 16);
        String          name        = _clazzName;
        ProjectState    state       = ProjectState.OKAY;
        // @formatter:on

        ProjectName projectName = new ProjectName(type, pd, category, id, name, state);
        Projects.getInstance().checkName(projectName);

        System.out.print("================================ ");
        System.out.println(projectName.getFullName());

        execute(projectName);

        System.out.println();
        System.out.println("SUCCESS");
    }

    private static void execute(ProjectName projectName) throws Exception {
        final int size = 7;
        int[][] code = new int[size][size];

        Random r1 = new Random(0x7e_030_dd5);
        Random r2 = new Random(0x7e_100_dd5);
        Random r3 = new Random(0x7e_002_dd5);

        for (int i = 0; i < size - 1; i++) {
            for (int j = 1; j < size - 1; j++) {
                code[i][j] = r1.nextBoolean() ^ r3.nextBoolean() ? r2.nextInt(0, 255) : r3.nextInt(1, 256);
            }
        }

        code[0][0] = 0x00;
        code[1][0] = 0x01;
        code[2][0] = 0x02;
        code[3][0] = 0x06;
        code[4][0] = 0x1a;
        code[5][0] = 0x97;
        code[6][0] = 0xFF;

        for (int i = 0; i < size; i++) {
            int vC = 0;
            int vR = 0;
            for (int j = 0; j < size - 1; j++) {
                vC += code[i][j];
                vR += code[j][i];
            }

            code[i][size - 1] = (size * size) * r1.nextInt(1, 7) - vC % (size * size);
            code[size - 1][i] = (size * size) * r2.nextInt(1, 7) - vR % (size * size);
        }

        for (int i = 0; i < size; i++) {
            int vC = 0;
            int vR = 0;
            for (int j = 0; j < size; j++) {
                vC += code[i][j];
                vR += code[j][i];
            }

            if (vC % (size * size) != 0) throw new RuntimeException();
            if (vR % (size * size) != 0) throw new RuntimeException();
        }

        try (PrintStream stream = new PrintStream(projectName.getFile(".txt"))) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    System.out.printf("%02x  ", code[i][j]);
                    stream.printf("%02x  ", code[i][j]);
                }
                System.out.println();
                stream.println();
            }
        }
    }
}