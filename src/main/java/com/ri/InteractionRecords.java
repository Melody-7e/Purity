package com.ri;

import com.ri.meta.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


// don't ask what this is, just to keep recode for /nice/ and /not nice/ things people does with me.
// UPDATE: NO, Well I changed my mind, I am not using it now.
public class InteractionRecords {
    @SuppressWarnings("UnnecessaryLocalVariable")
    public static void main(String[] args) throws Exception {
        // @formatter:off
        String          _clazzName  = InteractionRecords.class.getSimpleName();
        ProjectType     type        = ProjectType.ENGLISH;
        ProjectPD       pd          = ProjectPD.RIGHT;
        ProjectCategory category    = ProjectCategory.USABLE;
        byte            id          = (byte) 0xe5;
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
        File file = new File("C:\\Users\\91750\\My Drive\\InteractionRecords.txt"); // I don't want to keep it public

        if (!file.exists()) {
            throw new Exception("File doesn't exist");
        }

        // addEntry(file);
        printAll(file);
        // printFiltered(file, "Me");
        System.out.println("================================================");
        printSummary(file);
    }

    private static void addEntry(File file) throws Exception {
        // @formatter:off
        String             personCode   = "Melody~";
        int                daysBack     = 1;
        String             note         = "------------------ Records Starts from this Date ------------------";
        Interaction.Type   type         = Interaction.Type.OKAY;
        int                intensity    = 3; // 0-36
        // @formatter:on

        int days = Math.toIntExact(System.currentTimeMillis() / 1000 / 60 / 60 / 24) - daysBack;
        Interaction interaction = new Interaction(personCode, days, type, intensity, note);

        try (PrintStream out = new PrintStream(new FileOutputStream(file, true))) {
            out.println(interaction.serialize());
        }
    }

    private static ArrayList<Interaction> readEntries(File file) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            ArrayList<Interaction> interactions = new ArrayList<>();

            reader.lines().forEach(line -> {
                if (!line.startsWith("#") && !line.trim().isEmpty()) {
                    try {
                        interactions.add(Interaction.deserialize(line));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            return interactions;
        }
    }

    private static void printSummary(File file) throws Exception {
        ArrayList<Interaction> interactions = readEntries(file);

        Map<String, long[]> personMap = new HashMap<>();

        for (Interaction interaction : interactions) {
            long[] count = personMap.computeIfAbsent(interaction.personCode, _ -> new long[Interaction.Type.values().length]);

            count[interaction.type.ordinal()] += interaction.intensity;
        }

        for (int i = 0; i < Interaction.Type.values().length; i++) {
            System.out.print("    " + Interaction.Type.values()[i].code);
        }
        System.out.println();
        for (Map.Entry<String, long[]> entry : personMap.entrySet()) {
            for (int i = 0; i < entry.getValue().length; i++) {
                System.out.printf("%5d", entry.getValue()[i]);
            }

            System.out.println("    " + entry.getKey());
        }
    }

    private static void printAll(File file) throws Exception {
        ArrayList<Interaction> interactions = readEntries(file);

        for (Interaction interaction : interactions) {
            System.out.println(interaction);
        }
    }

    private static void printFiltered(File file, String personCode) throws Exception {
        ArrayList<Interaction> interactions = readEntries(file);

        for (Interaction interaction : interactions) {
            if (interaction.personCode.equals(personCode)) {
                System.out.println(interaction);
            }
        }
    }

    private record Interaction(String personCode, int days, Type type, int intensity, String note) {
        private Interaction {
            if (intensity < 0 || intensity > 36) {
                throw new IllegalArgumentException("intensity must be between 0 and 36");
            }
        }

        static Interaction deserialize(String str) {
            if (str.charAt(0) != ':') throw new IllegalArgumentException();

            String[] split = str.substring(1).split(" [| ]*", 6);

            if (split.length != 6) throw new IllegalArgumentException();

            String personCode = split[1].strip();
            int days = Integer.parseInt(split[2].strip(), 16);
            Type type = Type.fromCode(split[3].strip());
            int intensity = Integer.parseInt(split[4].strip(), 36);
            String noteStr = split[5].strip();
            return new Interaction(personCode, days, type, intensity, noteStr);
        }

        String serialize() {
            return String.format(": %20s %04X %c %s | %s", personCode, days, type.code, Integer.toString(intensity, 36), note);
        }

        @Override
        public String toString() {
            long ms = (long) days * 24 * 60 * 60 * 1000;

            return String.format(": %20s   %2$tb %2$td, %tY %12s  %2d | %s", personCode, ms, type, intensity, note);
        }

        enum Type {
            // @formatter:off
            SORRY       ('s'),  //   I did something /not nice/
            THANK_YOU   ('c'),  // YOU did something /nice/
            OKAY        ('o'),  //   I did something /nice/
            WHY         ('x');  // YOU did something /not nice/, simple
            // @formatter:on

            private static final Map<Character, Type> CODE_MAP;

            static {
                Map<Character, Type> map = new HashMap<>();
                for (Type intensity : values()) {
                    map.put(intensity.code, intensity);
                }
                CODE_MAP = Collections.unmodifiableMap(map);
            }

            final char code;

            Type(char code) {
                this.code = code;
            }

            public static Type fromCode(String code) {
                if (code.length() != 1) throw new IllegalArgumentException();
                return CODE_MAP.get(code.charAt(0));
            }
        }
    }
}
