// @formatter:off
package com.ri;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.Scanner;

// ([Ft1t2i+]# <2c> c7) Code775_c7
/** Failed:
 * Well this was supposed to be a password generated (but is not very nice, so I won't be using it for that),
 * but well I am thinking to make a better name generator similar to this,
 * but I don't have ideas yet. Till then let's just use {@link com.ri.meta.ProjectName} */
public final class Code775_c7 {
    private static final String[] SEQUENCE_FORMATE_H = new String[]{
            "123456789",
            "!@#$%^&*(",
            "QaWsEdRfT",
            "#E4r%T6y&"
    };

    private static final String[][] SEQUENCE_SL = new String[][]{
            new String[]{
                    "1213",
                    "12131214",
                    "1213121412131215"
            },
            new String[]{
                    "121",
                    "121343121",
                    "121343121565787565121343121"
            },
            new String[]{
                    "5",
                    "5645",
                    "5645342378675645"
            },
            new String[]{
                    "1618",         // phi
                    "31415927",     // pi
                    "271828182846"  // e
            }
    };

    private static final   char[] SEPARATOR_H    = new   char[]{'?',   '.',   '-',   ')'};
    private static final   char[] SUFFIX_START_S = new   char[]{'R',   'i',   'm',   'c'};
    private static final String[] SUFFIX_END_L   = new String[]{"126", "7e",  "~"};

    private static final     char END_SEPARATOR  = '|';
    private static final   String START          = "[:";
    private static final   String END            = "]";

    public static void main(String[] args) {
        if (args.length == 0) {
            printHelp();
            return;
        }

        try {
            String command = args[0];
            switch (command) {
                case "help", "--help", "-h" -> printHelp();
                case "generate"             -> generate(args);
                case "verify"               -> verify(args);
                default                     -> System.out.println("\033[31mInvalid Argument\033[0m");
            }
        } catch (RuntimeException e) {
            System.err.println("\033[31m" + e.getMessage() + "\033[0m");
            System.exit(1);
            throw new RuntimeException(e);
        }
    }

    private static void printHelp() {
        System.out.println("""
                           \033[32;1mCode775\033[0m
                           \033[34mValid Commands\033[0m:
                           \t\033[35mgenerate\033[3m c hash s l h\033[0m
                           \t\033[35mverify\033[3m   c hash s l h\033[0m
                           
                           \033[36mc\033[0m: Any alphabet (lowercase)
                           \033[36ms\033[0m: R, i, m, c  \033[37m// \033[34mrandom\033[0m
                           \033[36ml\033[0m: s, m, l     \033[37m// \033[34mdepth\033[0m
                           \033[36mh\033[0m: 1, !, A, _  \033[37m// \033[34m(1, !) for quick | (A, _) for safe\033[0m
                           
                           \033[36mhash\033[0m:
                           \tLove,   Melo   \033[37m// \033[34mentertainment\033[0m
                           \tGame,   Nice   \033[37m// \033[34mgaming\033[0m
                           \tCode,   Pure   \033[37m// \033[34mcoding\033[0m
                           \tEdge,   Hate   \033[37m// \033[34meducational\033[0m
                           \tTemp,   Fast   \033[37m// \033[34mtemporary\033[0m
                           \tOkay,   Cute   \033[37m// \033[34mother\033[0m
                           
                           \tMelody, Colors \033[37m// \033[34msignature\033[0m
                           \tCanvas, Artist \033[37m// \033[34mreserved\033[0m""");
    }

    private static void generate(String[] args) {
        String code          = getCode(args, false);
        String codeDecorated = getCode(args, true);

        System.out.println(codeDecorated);

        StringSelection selection = new StringSelection(code);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }

    private static void verify(String[] args) {
        String code          = getCode(args, false);
        String codeDecorated = getCode(args, true);

        System.out.print("--> ");
        Scanner scanner = new Scanner(System.in);
        String  inCode  = scanner.nextLine();

        if (code.equals(inCode)) {
            System.out.println("\033[A\033[K--> " + codeDecorated);
            System.out.println("\033[32mSuccess~\033[0m");

            StringSelection selection = new StringSelection(code);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        } else {
            System.out.println("--> " + codeDecorated);
            System.out.println("\033[31mFailed!\033[0m");
        }
    }

    private static String getCode(String[] args, boolean decorated) {
        if (args.length != 6) throw new RuntimeException("Invalid Number of Arguments");

        if (args[1].length() != 1) throw new RuntimeException("Invalid c");
        if (args[3].length() != 1) throw new RuntimeException("Invalid s");
        if (args[4].length() != 1) throw new RuntimeException("Invalid l");
        if (args[5].length() != 1) throw new RuntimeException("Invalid h");

        Hash hash;
        try {
            hash = Hash.valueOf(args[2]);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid hash");
        }

        char c = args[1].charAt(0);
        char s = args[3].charAt(0);
        char l = args[4].charAt(0);
        char h = args[5].charAt(0);

        return getCode(c, hash, s, l, h, decorated);
    }

    private static String getCode(char c, Hash hash, char s, char l, char h, boolean decorated) {
        int s_ = switch (s) {
            case 'R' -> 0;
            case 'i' -> 1;
            case 'm' -> 2;
            case 'c' -> 3;
            default  -> throw new IllegalArgumentException("Invalid s");
        };

        int l_ = switch (l) {
            case 's' -> 0;
            case 'm' -> 1;
            case 'l' -> 2;
            default  -> throw new IllegalArgumentException("Invalid l");
        };

        int h_ = switch (h) {
            case '1' -> 0;
            case '!' -> 1;
            case 'A' -> 2;
            case '_' -> 3;
            default  -> throw new IllegalArgumentException("Invalid h");
        };

        return getCode(c, hash, s_, l_, h_, decorated);
    }

    private static String getCode(char c, Hash hash, int s, int l, int h, boolean decorated) {
        if (c < 'a' || c > 'z') throw new IllegalArgumentException("Invalid c");
        if (s <  0  || s >  3)  throw new IllegalArgumentException("Invalid s");
        if (l <  0  || l >  2)  throw new IllegalArgumentException("Invalid l");
        if (h <  0  || h >  3)  throw new IllegalArgumentException("Invalid h");

        char[] sequenceRaw     = SEQUENCE_SL[s][l].toCharArray();
        String sequenceFormate = SEQUENCE_FORMATE_H[h];

        String suffix  = SUFFIX_START_S[s] + SUFFIX_END_L[l];
        char separator = SEPARATOR_H[h];

        StringBuilder sequence = new StringBuilder();
        for (char ch : sequenceRaw) {
            int index = ch - '1';
            sequence.append(sequenceFormate.charAt(index));
        }

        if (!decorated)
            return START + c + hash.toString() + separator + sequence + END_SEPARATOR + suffix + END;
        else
            return  "\033[32m" + START +
                    "\033[34m" + c + "\033[1m" + hash.toString() + "\033[0m" +
                    "\033[37m" + separator +
                    "\033[35m" + sequence +
                    "\033[37m" + END_SEPARATOR +
                    "\033[36m" + suffix +
                    "\033[32m" + END + "\033[0m";
    }

    private enum Hash {
        Love,   Melo,   // entertainment
        Game,   Nice,   // gaming
        Code,   Pure,   // coding
        Edge,   Hate,   // educational
        Temp,   Fast,   // temporary
        Okay,   Cute,   // other

        Melody, Colors, // signature
        Canvas, Artist, // reserved
    }
}