package com.ri.meta;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

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
            checkName(projectName, true);
        }
    }

    public void checkName(ProjectName projectName) {
        checkName(projectName, false);
    }

    private void checkName(ProjectName projectName, boolean add) {
        short id = (short) (projectName.getCategory().value() << 8 | projectName.getId());

        ProjectName old;

        if (add) {
            old = ids.put(id, projectName);
        } else {
            old = ids.get(id);
        }

        if (old != null) {
            if (!old.getType().equals(projectName.getType())) {
                throw new RuntimeException("Same category and id but different types, " + old + " and " + projectName);
            } else if (projectName.getName().equals(old.getName())) {
                if (!old.getPd().equals(projectName.getPd())) {
                    throw new RuntimeException("Same category and id and name but different pd, " + old + " and " + projectName);
                }
                if (!old.getState().equals(projectName.getState())) {
                    throw new RuntimeException("Same category and id and name but different state, " + old + " and " + projectName);
                }

                throw new RuntimeException("Project with same category and id and name already exist, " + old + " and " + projectName);
            }
            // else name can be different to mark variant/format
        }
    }

    public void add(ProjectName projectName) {
        checkName(projectName, true);
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

    public ArrayList<ProjectName> getByName(String name) {
        ArrayList<ProjectName> results = new ArrayList<>();
        for (ProjectName projectName : projectNames) {
            if (Objects.equals(projectName.getName(), name))
                results.add(projectName);
        }

        return results;
    }
}
