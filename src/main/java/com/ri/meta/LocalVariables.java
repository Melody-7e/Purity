package com.ri.meta;

import java.io.*;
import java.util.HashMap;

public class LocalVariables {
    public static final HashMap<String, String> values = new HashMap<>();

    private static final File file = new File("localVariables.txt");
    private static boolean initialized = false;

    public static String get(String key) {
        if (!initialized) initialize();
        return values.get(key);
    }

    private static void initialize() {
        initialized = true;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] split = line.split("=", 2);
                values.put(split[0].strip(), split[1].strip());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
