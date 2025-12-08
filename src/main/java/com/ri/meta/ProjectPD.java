package com.ri.meta;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// Purity Direction
public enum ProjectPD {
    // For LEFT_PURE, LEFT, LEFT_MID the first word must point to the src code and later to parameters or file shall be a .md or .txt with src code or src code location inside.
    // @formatter:off
    LEFT_PURE   ("i-"),     // Totally programmable, Pure and Universal
    LEFT        ("i"),      // Totally programmable
    LEFT_MID    ("i+"),     // Programmable but post edits or too much magic /numbers/
    CENTER      ("0"),      // Pure Mix of computable and human-only interactions (or unknown for some reason)
    RIGHT_MID   ("R-"),     // Human-Made but computer effects later
    RIGHT       ("R");      // Totally Human-Made.
    //@formatter:on

    private static final Map<String, ProjectPD> SYMBOL_MAP;

    static {
        Map<String, ProjectPD> map = new HashMap<>();
        for (ProjectPD pd : values()) {
            map.put(pd.symbol, pd);
        }
        SYMBOL_MAP = Collections.unmodifiableMap(map);
    }

    private final String symbol;

    ProjectPD(String symbol) {
        this.symbol = symbol;
    }

    public static ProjectPD fromSymbol(String symbol) {
        return SYMBOL_MAP.get(symbol);
    }

    public String symbol() {
        return symbol;
    }
}
