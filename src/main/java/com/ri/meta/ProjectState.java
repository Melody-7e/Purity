package com.ri.meta;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum ProjectState {
    OKAY(""),               // Nothing too special but don't know what to do next
    SUCCESS("~"),           // Successful, looks good, pure, usable or completed
    INCOMPLETE("#"),        // Feels good but something's missing don't know what or don't have skills/knowledge to complete it
    FAILED("!");            // Didn't get what I was expecting.

    private static final Map<String, ProjectState> SYMBOL_MAP;

    static {
        Map<String, ProjectState> map = new HashMap<>();
        for (ProjectState state : values()) {
            map.put(state.symbol, state);
        }
        SYMBOL_MAP = Collections.unmodifiableMap(map);
    }

    private final String symbol;

    ProjectState(String symbol) {
        this.symbol = symbol;
    }

    public static ProjectState fromSymbol(String symbol) {
        return SYMBOL_MAP.get(symbol);
    }

    public String symbol() {
        return symbol;
    }
}
