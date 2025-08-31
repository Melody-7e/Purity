package com.ri.meta;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum ProjectCategory {
    // @formatter:off
    VOID                (0x00),             // VOID exists for no reason
    SIGNATURE           (0x26),             // Something I found all by myself and consider as signature
    MELODY              (0x7e),             // My paracosm (imaginary world) or F/B memory (Forbidden Memory, some \shall not exist\ memories I somehow have)
    INFINITY            (0xff);             // Something that is universal and valuable (definitely, will not be used)
    // @formatter:on

    private static final Map<Byte, ProjectCategory> VALUE_MAP;

    static {
        Map<Byte, ProjectCategory> map = new HashMap<>();
        for (ProjectCategory category : values()) {
            map.put(category.value, category);
        }
        VALUE_MAP = Collections.unmodifiableMap(map);
    }

    private final byte value;

    ProjectCategory(int value) {
        this.value = (byte) value;
    }

    public static ProjectCategory fromValue(byte value) {
        return VALUE_MAP.get(value);
    }

    public int value() {
        return value;
    }
}
