package com.ri.meta;

import static com.ri.meta.ProjectType.ProjectTypeCode.*;

import java.util.*;

public class ProjectType {
    // @formatter:off
    // PRIMITIVES
    public static final ProjectType OTHER              = new ProjectType(MISC,                  0);
    public static final ProjectType VECTOR_IMG         = new ProjectType(VECTOR,                2);
    public static final ProjectType MODEL_3D           = new ProjectType(VECTOR,                3);
    public static final ProjectType SOUND              = new ProjectType(DIMENSION_VISUAL,      1);
    public static final ProjectType IMAGE              = new ProjectType(DIMENSION_VISUAL,      2);
    public static final ProjectType VIDEO              = new ProjectType(DIMENSION_VISUAL,      3);
    public static final ProjectType NULL               = new ProjectType(NUMBER,                0);
    public static final ProjectType INT_NUMBER         = new ProjectType(NUMBER,                1);
    public static final ProjectType REAL_NUMBER        = new ProjectType(NUMBER,                2);
    public static final ProjectType COMPLEX_NUMBER     = new ProjectType(NUMBER,                3);
    public static final ProjectType CODE               = new ProjectType(TEXT,                  0);
    public static final ProjectType PATTERN            = new ProjectType(TEXT,                  1);
    public static final ProjectType ENGLISH            = new ProjectType(TEXT,                  2);


    // COMMONLY USED COMPLEXES
    public static final ProjectType INT_SERIES         = new ProjectType(FUNCTION,      INT_NUMBER,         INT_NUMBER);
    public static final ProjectType RI_FUNCTION        = new ProjectType(FUNCTION,      REAL_NUMBER,        INT_NUMBER); // !coincidence ri
    public static final ProjectType CC_FUNCTION        = new ProjectType(FUNCTION,      COMPLEX_NUMBER,     COMPLEX_NUMBER);
    public static final ProjectType IMAGE_EFFECT       = new ProjectType(FUNCTION,      IMAGE,              IMAGE);

    public static final ProjectType EASYEDIT_IMAGE     = new ProjectType(EASY_EDIT,     IMAGE);
    public static final ProjectType EASYEDIT_VEC_IMG   = new ProjectType(EASY_EDIT,     VECTOR_IMG);
    public static final ProjectType EASYEDIT_3D        = new ProjectType(EASY_EDIT,     MODEL_3D);
    // @formatter:on

    private final ProjectTypeCode code;
    private final ProjectType[] child;
    private final int i;

    public ProjectType(ProjectTypeCode code, int i) {
        if (code.size != -1)
            throw new IllegalArgumentException("Not a primitive code " + code);
        if ((i < code.min || i >= code.max))
            throw new IllegalArgumentException("Illegal `i=" + i + "` for " + code);

        this.code = code;
        this.child = null;
        this.i = i;
    }

    public ProjectType(ProjectTypeCode code, ProjectType... child) {
        if (code.size != child.length)
            throw new IllegalArgumentException("Illegal number of child (" + child.length + ") for " + code);

        this.code = code;
        this.child = child;
        this.i = -1;
    }

    private static ProjectType valueOfRaw(String string, int[] index) {
        char ch = string.charAt(index[0]++);

        boolean parenthesisUsed;
        if (ch == '(') {
            parenthesisUsed = true;
            ch = string.charAt(index[0]++);
        } else {
            parenthesisUsed = false;
        }

        ProjectTypeCode c = ProjectTypeCode.fromSymbol(ch);

        if (c == null)
            throw new IllegalArgumentException("Illegal code '" + ch + "'");

        if (c.size == -1) {
            int i = Character.getNumericValue(string.charAt(index[0]++));

            if (parenthesisUsed) {
                if (string.charAt(index[0]++) != ')') throw new IllegalArgumentException("Invalid Parenthesis");
            }

            return new ProjectType(c, i);
        }

        ProjectType[] child = new ProjectType[c.size];
        for (int i = 0; i < c.size; i++) {
            child[i] = valueOf(string, index);
        }

        if (parenthesisUsed) {
            if (string.charAt(index[0]++) != ')') throw new IllegalArgumentException("Invalid Parenthesis");
        }

        return new ProjectType(c, child);
    }

    public static ProjectType valueOf(String string, int[] index) {
        return valueOfRaw(string.toLowerCase(Locale.ROOT), index);
    }

    public static ProjectType valueOf(String string) {
        int[] index = new int[1];
        ProjectType value = valueOf(string, index);

        if (index[0] != string.length())
            throw new IllegalArgumentException("Expected size of " + index[0] + ", found " + string.length());
        return value;
    }

    private void getCode(StringBuilder s) {
        boolean isRoot = s.isEmpty();

        if (child == null) {
            s.append(code.symbol).append(i);
        } else {
            if (!isRoot) {
                s.append('(');
                s.append(Character.toUpperCase(code.symbol));
            } else {
                s.append(code.symbol);
            }

            for (ProjectType t : child) {
                t.getCode(s);
            }

            if (!isRoot) s.append(')');
        }
    }

    public String getCode() {
        StringBuilder s = new StringBuilder();
        getCode(s);
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProjectType that = (ProjectType) o;
        return i == that.i && code == that.code && Objects.deepEquals(child, that.child);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, Arrays.hashCode(child), i);
    }

    @Override
    public String toString() {
        return getCode();
    }

    public enum ProjectTypeCode {
        // @formatter:off
        MISC                ('m', 0, 4), // 0: OTHER
        VECTOR              ('v', 2, 4), //                                 2: VECTOR_IMG   3: 3D_MODEL
        DIMENSION_VISUAL    ('d', 1, 4), //                 1: SOUND        2: IMAGE        3: VIDEO
        NUMBER              ('n', 0, 4), // 0: NULL         1: INTEGER      2: REAL         3: COMPLEX
        TEXT                ('t', 0, 3), // 0: CODE         1: PATTERN      2: ENGLISH

        FUNCTION            ('f', 2),    // (INPUT, OUTPUT)     function that takes INPUT and gives OUTPUT
        EASY_EDIT           ('e', 1);    // (TYPE)              a `type` that let you easily make changes to the TYPE object.
        // @formatter:on

        private static final Map<Character, ProjectTypeCode> SYMBOL_MAP;

        static {
            Map<Character, ProjectTypeCode> map = new HashMap<>();
            for (ProjectTypeCode code : values()) {
                map.put(code.symbol, code);
            }
            SYMBOL_MAP = Collections.unmodifiableMap(map);
        }

        final char symbol;
        final int min;
        final int max;
        final int size;

        ProjectTypeCode(char code, int min, int max) {
            this.symbol = code;
            this.min = min;
            this.max = max;
            this.size = -1;
        }

        ProjectTypeCode(char code, int size) {
            this.symbol = code;
            this.min = -1;
            this.max = -1;
            this.size = size;
        }

        public static ProjectTypeCode fromSymbol(char symbol) {
            return SYMBOL_MAP.get(symbol);
        }
    }
}
