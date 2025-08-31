package com.ri.meta;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Projects {
    public static final File PROJECT_DIR = new File("projects/");

    private static Projects instance;

    private final ArrayList<ProjectName> projectNames;
    private HashMap<Short, ProjectName> ids;

    public Projects(ArrayList<ProjectName> projectNames) {
        this.projectNames = projectNames;

        validate();
    }

    public Projects(File projectDir) {
        String[] files = projectDir.list();

        if (files == null) throw new RuntimeException("Specified project directory is not a directory.");

        projectNames = new ArrayList<>(files.length);
        for (String file : files) {
            try {
                projectNames.add(ProjectName.valueOf(file));
            } catch (RuntimeException e) {
                throw new RuntimeException("Cannot parse file name, " + file, e);
            }
        }

        validate();
    }

    public Projects() {
        this(PROJECT_DIR);
    }

    public static Projects getInstance() {
        if (instance != null) return instance;
        return instance = new Projects();
    }

    private void validate() {
        ids = new HashMap<>(projectNames.size());
        for (ProjectName projectName : projectNames) {
            checkName(projectName);
        }
    }

    public void checkName(ProjectName projectName) {
        short id = (short) (projectName.getCategory().value() << 8 | projectName.getId());

        ProjectName old = ids.put(id, projectName);

        if (old != null) {
            if (!old.getType().equals(projectName.getType())) {
                throw new RuntimeException("Same category and id but different types, " + old + " and " + projectName);
            }
            if (projectName.getName().equals(old.getName())) {
                if (!old.getPd().equals(projectName.getPd())) {
                    throw new RuntimeException("Same category and id but different pd, " + old + " and " + projectName);
                }
                if (!old.getState().equals(projectName.getState())) {
                    throw new RuntimeException("Same category and id but different state, " + old + " and " + projectName);
                }
            }
            // else name can be different to mark variant/format
        }
    }

    public void add(ProjectName projectName) {
        checkName(projectName);
        projectNames.add(projectName);
    }

    public ArrayList<ProjectName> getByPd(ProjectPD pd) {
        ArrayList<ProjectName> results = new ArrayList<>();
        for (ProjectName projectName : projectNames) {
            if (projectName.getPd().equals(pd))
                results.add(projectName);
        }

        return results;
    }

    public ArrayList<ProjectName> getByState(ProjectState state) {
        ArrayList<ProjectName> results = new ArrayList<>();
        for (ProjectName projectName : projectNames) {
            if (projectName.getState().equals(state))
                results.add(projectName);
        }

        return results;
    }

    public ArrayList<ProjectName> getByType(ProjectType type) {
        ArrayList<ProjectName> results = new ArrayList<>();
        for (ProjectName projectName : projectNames) {
            if (projectName.getType().equals(type))
                results.add(projectName);
        }

        return results;
    }

    public ArrayList<ProjectName> getByCategory(byte category) {
        ArrayList<ProjectName> results = new ArrayList<>();
        for (ProjectName projectName : projectNames) {
            if (projectName.getCategory().value() == category)
                results.add(projectName);
        }

        return results;
    }

    public ArrayList<ProjectName> getById(byte category, byte id) {
        ArrayList<ProjectName> results = new ArrayList<>();
        for (ProjectName projectName : projectNames) {
            if (projectName.getCategory().value() == category && projectName.getId() == id)
                results.add(projectName);
        }

        return results;
    }

    public ProjectName getName(String name) {
        ArrayList<ProjectName> results = new ArrayList<>();
        for (ProjectName projectName : projectNames) {
            if (projectName.getName().equals(name))
                return projectName;
        }

        return null;
    }
}
