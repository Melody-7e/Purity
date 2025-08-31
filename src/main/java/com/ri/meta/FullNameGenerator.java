package com.ri.meta;

import java.util.Arrays;
import java.util.Scanner;

public class FullNameGenerator {
    public static void main(String[] args) {
        ProjectType type;
        ProjectPD pd;
        ProjectCategory category;
        byte id;
        String name;
        ProjectState state;

        Scanner scanner = new Scanner(System.in);

        for (; ; ) {
            System.out.println();
            System.out.print("Type: ");
            String string = scanner.nextLine();

            try {
                type = ProjectType.valueOf(string);
                break;
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        for (; ; ) {
            System.out.println();
            System.out.print("Direction: ");
            String string = scanner.nextLine();

            try {
                pd = ProjectPD.valueOf(string);
                break;
            } catch (Exception e) {
                System.err.println("Choose from " + Arrays.toString(ProjectPD.values()));
            }
        }

        for (; ; ) {
            System.out.println();
            System.out.print("Category: ");
            String string = scanner.nextLine();

            try {
                category = ProjectCategory.valueOf(string);
                break;
            } catch (Exception e) {
                System.err.println("Choose from " + Arrays.toString(ProjectCategory.values()));
            }
        }

        for (; ; ) {
            System.out.println();
            System.out.print("ID: ");
            String string = scanner.nextLine();

            try {
                int idInt = Integer.parseInt(string, 16);

                if (idInt < 0 || idInt > 0xFF) {
                    System.out.println("Out of range 00-FF");
                    continue;
                }

                id = (byte) idInt;
                break;
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        {
            System.out.println();
            System.out.print("Name: ");
            String string = scanner.nextLine();

            if (string.isBlank()) {
                name = RandomNameGenerator.getNewName();
            } else {
                name = string.trim();
            }
        }

        for (; ; ) {
            System.out.println();
            System.out.print("State: ");
            String string = scanner.nextLine();

            try {
                state = ProjectState.valueOf(string);
                break;
            } catch (Exception e) {
                System.err.println("Choose from " + Arrays.toString(ProjectState.values()));
            }
        }

        ProjectName projectName = new ProjectName(type, pd, category, id, name, state);

        System.out.println();
        System.out.println();
        System.out.println("Full Name: " + projectName.getFullName());
        System.out.println("URL Safe Name: " + projectName.getUrlSafeName());
    }
}
