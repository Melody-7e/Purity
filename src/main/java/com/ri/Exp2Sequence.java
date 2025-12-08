package com.ri;

import static com.ri.helper.PurityMaths.log2;

import com.ri.meta.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

// "Binary Exponential Recursion Sequence"
public class Exp2Sequence {
    private static final BigDecimal[] samples1 = new BigDecimal[]{
            BigDecimal.valueOf(-5),
            BigDecimal.valueOf(-4),
            BigDecimal.valueOf(-3),
            BigDecimal.valueOf(-2),
            BigDecimal.valueOf(-1),
            BigDecimal.valueOf(0),
            BigDecimal.valueOf(1),
            BigDecimal.valueOf(2),
            BigDecimal.valueOf(3),
            BigDecimal.valueOf(4),
            BigDecimal.valueOf(5),
            BigDecimal.valueOf(6),
            BigDecimal.valueOf(7),
            BigDecimal.valueOf(8),
            BigDecimal.valueOf(9),
            BigDecimal.valueOf(10),
            BigDecimal.valueOf(11),
            BigDecimal.valueOf(12),
            BigDecimal.valueOf(13),
            BigDecimal.valueOf(14),
            BigDecimal.valueOf(15),
            BigDecimal.valueOf(16),
            BigDecimal.valueOf(17),
            null,
            BigDecimal.valueOf(0.5),
            BigDecimal.valueOf(0.25),
            BigDecimal.valueOf(0.125),
            BigDecimal.valueOf(0.0625),
            BigDecimal.valueOf(0.875),
            BigDecimal.valueOf(0.75),
            BigDecimal.valueOf(1.0),
            BigDecimal.valueOf(1.5),
            BigDecimal.valueOf(1.25),
            BigDecimal.valueOf(2.0),
            BigDecimal.valueOf(2.5),
            BigDecimal.valueOf(2.25),
            BigDecimal.valueOf(-1.0),
            BigDecimal.valueOf(-0.5),
            BigDecimal.valueOf(-0.75),
            BigDecimal.valueOf(1.33),
            BigDecimal.valueOf(0.3),
            BigDecimal.valueOf(0.1),
    };

    public static void main(String[] args) throws Exception {
        // @formatter:off
        ProjectType     type        = ProjectType.IR_FUNCTION;
        ProjectPD       pd          = ProjectPD.LEFT;
        ProjectCategory category    = ProjectCategory.SIGNATURE;
        byte            id          = (byte) 0x42;
        String          name        = Exp2Sequence.class.getSimpleName();
        ProjectState    state       = ProjectState.SUCCESS;
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
        BufferedWriter writer = new BufferedWriter(new FileWriter(projectName.getFile("txt")));

        for (BigDecimal i : samples1) {
            if (i != null) {
                Integer fi = f(i);

                if (fi != null) {
                    writer.write(String.format("%12s -> %2d", i, fi));
                } else {
                    writer.write(String.format("%12s -> %2s", i, "inf"));
                }
            }
            writer.newLine();
        }

        writer.close();
    }

    public static Integer f(BigDecimal x) {
        if (x.signum() == 0) {
            return null;
        }

        if (x.stripTrailingZeros().scale() <= 0) {
            BigInteger intValue = x.toBigInteger();
            return intValue.getLowestSetBit();

        } else {
            int power = 0;
            BigDecimal currentX = x;

            while (currentX.stripTrailingZeros().scale() > 0) {
                currentX = currentX.multiply(BigDecimal.TWO);
                power++;

                if (power == Short.MAX_VALUE) return null;
            }
            return -power;
        }
    }

    public static int f(int x) {
        return (x != 0) ? log2(-x & x) : -1;
    }
}
