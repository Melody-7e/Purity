package com.ri;

import com.ri.meta.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;


public class Permu {
    private static final Permu permu_16 = new Permu((byte) 0xc2, "4d-123-5c-6b-98a-0f-7e");
    private static final Permu permu_24 = new Permu((byte) 0xc4, "7D-5C-EFG-I0-M-A4-B3-H-LN-98-J-126-K");

    private final byte id;
    private final String string;
    private final int[] permutation;

    private Permu(byte id, String string) {
        this.id = id;
        this.string = string;

        ArrayList<Integer> permutationList = new ArrayList<>();
        char[] chars = string.toCharArray();
        for (char c : chars) {
            if (c == '-') continue;

            permutationList.add(Character.getNumericValue(c));
        }

        permutation = new int[permutationList.size()];
        for (int i = 0, length = permutationList.size(); i < length; i++) {
            permutation[i] = permutationList.get(i);
        }
    }

    public static void main(String[] args) throws Exception {
        permu_16.writeFile();
        permu_24.writeFile();
    }

    private void writeFile() throws Exception {
        // @formatter:off
        ProjectType     type        = ProjectType.PATTERN;
        ProjectPD       pd          = ProjectPD.LEFT;
        ProjectCategory category    = ProjectCategory.SIGNATURE;
        byte            id          = this.id;
        String          name        = Permu.class.getSimpleName() + ' ' + permutation.length;
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

    private void execute(ProjectName projectName) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(projectName.getFile("txt")));
        writer.write(this.string);
        writer.close();
    }

    private int[] wrap(int[] data) {
        if (data.length != permutation.length) throw new RuntimeException();

        int[] newData = new int[permutation.length];
        for (int i = 0; i < permutation.length; i++) {
            newData[i] = data[permutation[i]];
        }

        return newData;
    }

    private String wrap(String data) {
        if (data.length() != permutation.length) throw new RuntimeException();

        StringBuilder newData = new StringBuilder(permutation.length);
        for (int p : permutation) {
            newData.append(data.charAt(p));
        }

        return newData.toString();
    }

    @Override
    public String toString() {
        return string;
    }
}
