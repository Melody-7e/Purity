package com.ri.meta;

import static com.ri.meta.Projects.PROJECT_DIR;

import java.io.File;
import java.util.Objects;

// state([Type] <category> id) Name
public class ProjectName {
    private final ProjectType type;
    private final ProjectPD pd;
    private final ProjectCategory category;
    private final byte id;
    private final String name;
    private final ProjectState state;

    private final String fullName;
    private final String urlSafeName;

    public ProjectName(ProjectType type, ProjectPD pd, ProjectCategory category, byte id, String name, ProjectState state) {
        // @formatter:off
        this.type     = type;
        this.pd       = pd;
        this.category = category;
        this.id       = id;
        this.name     = (name == null) ? null : name.trim();
        this.state    = state;
        // @formatter:on

        fullName = String.format("([%s%s]%s <%02x> %02x) %s", type.getCode(), pd.symbol(), state.symbol(), category.value(), id, this.name).trim();
        urlSafeName = String.format("([%s%s]%s %02x-%02x) %s", type.getCode(), pd.symbol(), state.symbol(), category.value(), id, this.name).trim();
    }

    public static ProjectName valueOf(String string) {
        ProjectType type;
        ProjectPD pd;
        ProjectCategory category;
        byte id;
        String name;
        ProjectState state;

        String[] topSplit = string.split("[()]", 3);

        name = topSplit[2].trim();
        if (name.isEmpty()) name = null;

        String baseName = topSplit[1];

        if (!"".equals(topSplit[0])) throw new RuntimeException("Invalid Name");


        String[] bottomSplit = baseName.split("[ \\-]", 3);
        String typePdStateString = bottomSplit[0];
        String categoryString = bottomSplit[1];
        String idString = bottomSplit[2];

        {
            int brIndex = typePdStateString.indexOf(']');
            if (!(typePdStateString.startsWith("[") && brIndex != -1)) throw new RuntimeException("Invalid Name");

            int[] index = new int[]{1};
            type = ProjectType.valueOf(typePdStateString, index);

            String pdString = typePdStateString.substring(index[0], brIndex);
            pd = ProjectPD.fromSymbol(pdString);

            String stateString = typePdStateString.substring(brIndex + 1);
            state = ProjectState.fromSymbol(stateString);
        }

        {
            if (categoryString.startsWith("<") && categoryString.endsWith(">"))
                categoryString = categoryString.substring(1, categoryString.length() - 1);

            category = ProjectCategory.fromValue((byte) Integer.parseInt(categoryString, 16));
        }

        {
            id = (byte) Integer.parseInt(idString, 16);
        }

        return new ProjectName(type, pd, category, id, name, state);
    }

    public ProjectType getType() {
        return type;
    }

    public ProjectPD getPd() {
        return pd;
    }

    public ProjectCategory getCategory() {
        return category;
    }

    public byte getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ProjectState getState() {
        return state;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUrlSafeName() {
        return urlSafeName;
    }

    public File getFile(String extension) {
        if (extension.startsWith(".")) extension = extension.substring(1);

        int i = 0;
        File file;
        do {
            file = new File(PROJECT_DIR, urlSafeName + ((i!=0) ? " #" + i : "") + '.' + extension);
            i++;
        } while (file.exists());

        return file;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProjectName that = (ProjectName) o;
        return Objects.equals(fullName, that.fullName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fullName);
    }

    @Override
    public String toString() {
        return fullName;
    }
}
